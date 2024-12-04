package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.enums.AssetType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {
    private Long id;
    private String symbol;
    private String name;
    private AssetType type;
    private Double currentPrice;
    private Double change;
    private Double changePercent;
    private Long volume;
    private LocalDateTime lastUpdated;

    public static AssetDTO fromEntity(Asset asset) {
        return AssetDTO.builder()
                .id(asset.getId())
                .symbol(asset.getSymbol())
                .name(asset.getName())
                .build();
    }
}