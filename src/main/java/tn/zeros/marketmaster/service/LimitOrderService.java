package tn.zeros.marketmaster.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.LimitOrderDTO;
import tn.zeros.marketmaster.dto.TransactionDTO;

import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.LimitOrder;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.enums.OrderStatus;
import tn.zeros.marketmaster.entity.enums.TransactionType;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.AssetRepository;
import tn.zeros.marketmaster.repository.LimitOrderRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class LimitOrderService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final LimitOrderRepository limitOrderRepository;
    private final TransactionService transactionService;
    private final AssetService assetService;

    public LimitOrderDTO AddLimitOrder(Long userId , LimitOrderDTO limitOrderDTO) {

        LimitOrder limitOrder =limitOrderDTO.toEntity();
        Asset asset = assetRepository.findBySymbol(limitOrderDTO.getSymbol());
        limitOrder.setAsset(asset);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        limitOrder.setUser(user);
        limitOrderRepository.save(limitOrder);
        return limitOrderDTO;
    }

    @Scheduled(fixedRate = 2000)
    @Transactional
    public void syncLimitOrder(){
        List<LimitOrder> limitOrders = limitOrderRepository.findAll();
        for(LimitOrder limitOrder : limitOrders){
            if (limitOrder.getLimitPrice()>=assetService.getCurrentPrice(limitOrder.getAsset().getId())&&limitOrder.getStatus().equals(OrderStatus.PENDING)){
                TransactionDTO transactionDTO = new TransactionDTO();
                transactionDTO.setSymbol(limitOrder.getAsset().getSymbol());
                transactionDTO.setQuantity(limitOrder.getQuantity());
                transactionDTO.setType(limitOrder.getType());
                Long userId = limitOrder.getUser().getId();
                limitOrder.setExecutionTimestamp(LocalDateTime.now());
                limitOrder.setStatus(OrderStatus.EXECUTED);
                limitOrderRepository.save(limitOrder);
                transactionService.ajoutUneTransaction(userId, transactionDTO);
            } else if (limitOrder.getStatus().equals(OrderStatus.CANCELLED)) {
                limitOrderRepository.delete(limitOrder);
            }

        }

    }
}
