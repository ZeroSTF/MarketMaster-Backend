package tn.zeros.marketmaster.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.zeros.marketmaster.entity.Holding;
import tn.zeros.marketmaster.repository.HoldingRepository;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;

    public HoldingService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }

    @Transactional
    public void updateAverageCostBasis(Long holdingId, BigDecimal newPurchaseAmount, int newQuantity) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found"));

        BigDecimal totalCost = holding.getAverageCostBasis()
                .multiply(BigDecimal.valueOf(holding.getQuantity()))
                .add(newPurchaseAmount);

        int totalQuantity = holding.getQuantity() + newQuantity;

        BigDecimal newAverageCostBasis = totalCost.divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP);

        holding.setAverageCostBasis(newAverageCostBasis);
        holding.setQuantity(totalQuantity);

        holdingRepository.save(holding);
    }
}