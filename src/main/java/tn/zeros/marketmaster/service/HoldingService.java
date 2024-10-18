package tn.zeros.marketmaster.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.zeros.marketmaster.dto.HoldingDTO;
import tn.zeros.marketmaster.entity.Holding;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.HoldingRepository;
import tn.zeros.marketmaster.repository.PortfolioRepository;
import tn.zeros.marketmaster.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;
    @Transactional
    public void updateAverageCostBasis(Long holdingId, BigDecimal newPurchaseAmount, int newQuantity) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found"));
        BigDecimal newFullAmount=newPurchaseAmount.multiply(BigDecimal.valueOf(newQuantity));
        BigDecimal totalCost = holding.getAverageCostBasis()
                .multiply(BigDecimal.valueOf(holding.getQuantity()))
                .add(newFullAmount);
        int totalQuantity = holding.getQuantity() + newQuantity;
        BigDecimal newAverageCostBasis = totalCost.divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP);

        holding.setAverageCostBasis(newAverageCostBasis);
        holding.setQuantity(totalQuantity);

        holdingRepository.save(holding);
    }
    public List<HoldingDTO> getAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Portfolio portfolio = user.getPortfolio();
        Set<Holding> holdings =portfolio.getHoldings();
        List<HoldingDTO> holdingDTOS = holdings.stream()
                .map(HoldingDTO::fromEntity)  //
                .collect(Collectors.toList());
        return holdingDTOS;
    }


}