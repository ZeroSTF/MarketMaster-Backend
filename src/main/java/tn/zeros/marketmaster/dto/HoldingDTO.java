package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Holding;

import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingDTO {
    private Long id;
    private Long portfolioId;
    private Long stockId;
    private String stockSymbol;  // Added for convenience
    private String stockName;    // Added for convenience
    private Integer quantity;
    private Double purchasePrice;
    private Double currentValue;
    private LocalDateTime purchasedAt;
    public static HoldingDTO fromEntity(Holding holding) {
        return HoldingDTO.builder()
                .id(holding.getId())
                .portfolioId(holding.getPortfolio().getId())
                .stockId(holding.getStock().getId())
                .stockSymbol(holding.getStock().getSymbol())
                .stockName(holding.getStock().getName())
                .quantity(holding.getQuantity())
                .purchasePrice(holding.getPurchasePrice())
                .currentValue(holding.getCurrentValue())
                .purchasedAt(holding.getPurchasedAt())
                .build();
    }
}
