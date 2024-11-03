package tn.zeros.marketmaster.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.LimitOrderDTO;
import tn.zeros.marketmaster.dto.TransactionDTO;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.LimitOrder;
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

    public LimitOrderDTO addLimitOrder(String username , LimitOrderDTO limitOrderDTO) {

        LimitOrder limitOrder =limitOrderDTO.toEntity();
        Asset asset = assetRepository.findBySymbol(limitOrderDTO.getSymbol());
        limitOrder.setAsset(asset);
        User user = userRepository.findByUsername(username)
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
            if (limitOrder.getStatus().equals(OrderStatus.PENDING)){
                if (limitOrder.getType().equals(TransactionType.BUY)){
                    if (limitOrder.getLimitPrice()<=assetService.getCurrentPrice(limitOrder.getAsset().getId())){
                        affectLimitOrder(limitOrder);
                    }
                }else if (limitOrder.getLimitPrice()>=assetService.getCurrentPrice(limitOrder.getAsset().getId())){
                    affectLimitOrder(limitOrder);
                }
            } else if (limitOrder.getStatus().equals(OrderStatus.CANCELLED)) {
                limitOrderRepository.delete(limitOrder);
            }

        }

    }
    public void affectLimitOrder(LimitOrder limitOrder) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setSymbol(limitOrder.getAsset().getSymbol());
        transactionDTO.setQuantity(limitOrder.getQuantity());
        transactionDTO.setType(limitOrder.getType());
        String userName = limitOrder.getUser().getUsername();
        limitOrder.setExecutionTimestamp(LocalDateTime.now());
        limitOrder.setStatus(OrderStatus.EXECUTED);
        limitOrderRepository.save(limitOrder);
        transactionService.addTransaction(userName, transactionDTO);
    }
}
