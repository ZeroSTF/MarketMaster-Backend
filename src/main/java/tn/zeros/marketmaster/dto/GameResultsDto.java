package tn.zeros.marketmaster.dto;

import java.math.BigDecimal;
import java.util.List;

public class GameResultsDto {
    private BigDecimal finalCash;
    private List<GameHoldingDto> holdings; // Use GameHoldingDto here
    private BigDecimal totalHoldingsValue;
    private BigDecimal profitPercentage;

    // Getters and Setters
    public BigDecimal getFinalCash() {
        return finalCash;
    }

    public void setFinalCash(BigDecimal finalCash) {
        this.finalCash = finalCash;
    }

    public List<GameHoldingDto> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<GameHoldingDto> holdings) {
        this.holdings = holdings;
    }

    public BigDecimal getTotalHoldingsValue() {
        return totalHoldingsValue;
    }

    public void setTotalHoldingsValue(BigDecimal totalHoldingsValue) {
        this.totalHoldingsValue = totalHoldingsValue;
    }

    public BigDecimal getProfitPercentage() {
        return profitPercentage;
    }

    public void setProfitPercentage(BigDecimal profitPercentage) {
        this.profitPercentage = profitPercentage;
    }
}