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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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


    public void affectLimitOrder(LimitOrder limitOrder) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setSymbol(limitOrder.getAsset().getSymbol());
        transactionDTO.setQuantity(limitOrder.getQuantity());
        transactionDTO.setType(limitOrder.getType());
        transactionDTO.setPrice(assetService.getCurrentPrice(limitOrder.getAsset().getId()));
        String userName = limitOrder.getUser().getUsername();
        limitOrder.setExecutionTimestamp(LocalDateTime.now());
        limitOrder.setStatus(OrderStatus.EXECUTED);
        limitOrderRepository.save(limitOrder);
        transactionService.addTransaction(userName, transactionDTO);
    }

    public List<LimitOrderDTO> getAllLimitOrders(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username: " + username + " not found."));

        List<LimitOrder> limitOrders = limitOrderRepository.findByUserId(user.getId());

        return limitOrders.stream()
                .map(LimitOrderDTO::fromEntity)
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteLimitOrder(String username,LimitOrderDTO limitOrderDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username: " + username + " not found."));
        Asset asset = assetRepository.findBySymbol(limitOrderDTO.getSymbol());
        limitOrderRepository.deleteLimitOrderByAssetAndLimitPriceAndUser(asset, limitOrderDTO.getLimitPrice(),user);
    }
    @Scheduled(fixedRate = 2000)
    @Transactional
    public void syncLimitOrder(){
        List<LimitOrder> limitOrders = limitOrderRepository.findAll();

        for (LimitOrder limitOrder : limitOrders) {

            if (limitOrder.getStatus().equals(OrderStatus.PENDING)) {


                if (limitOrder.getType().equals(TransactionType.BUY)) {
                    if (limitOrder.getLimitPrice() >= assetService.getCurrentPrice(limitOrder.getAsset().getId())) {
                        affectLimitOrder(limitOrder);
                    }
                }
                else if (limitOrder.getType().equals(TransactionType.SELL)) {
                    if (limitOrder.getLimitPrice() <= assetService.getCurrentPrice(limitOrder.getAsset().getId())) {
                        affectLimitOrder(limitOrder);
                    }
                }
            }
            else if (limitOrder.getStatus().equals(OrderStatus.CANCELLED)) {
                limitOrderRepository.delete(limitOrder);
            }
        }

    }
}
