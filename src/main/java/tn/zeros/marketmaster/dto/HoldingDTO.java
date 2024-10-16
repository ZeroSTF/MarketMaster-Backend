package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Holding;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingDTO {
    private Long id;
    private Long portfolioId;
    private Long assetId;
    private String assetSymbol;
    private String assetName;
    private Integer quantity;

    public static HoldingDTO fromEntity(Holding holding) {
        return HoldingDTO.builder()
                .id(holding.getId())
                .portfolioId(holding.getPortfolio() != null ? holding.getPortfolio().getId() : null)
                .assetId(holding.getAsset() != null ? holding.getAsset().getId() : null)
                .assetSymbol(holding.getAsset() != null ? holding.getAsset().getSymbol() : null)
                .assetName(holding.getAsset() != null ? holding.getAsset().getName() : null)
                .quantity(holding.getQuantity())
                .build();
    }

    public Holding toEntity() {
        Holding holding = new Holding();
        holding.setId(this.id);
        holding.setQuantity(this.quantity);
        return holding;
    }
}
