package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.zeros.marketmaster.entity.GameHolding;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GameHoldingDto {
    private String symbol;
    private int quantity;
    private BigDecimal averageCostBasis;

    public GameHoldingDto(GameHolding holding) {
        this.symbol = holding.getAsset().getSymbol();
        this.quantity = holding.getQuantity();
        this.averageCostBasis = holding.getAverageCostBasis();
    }
}

