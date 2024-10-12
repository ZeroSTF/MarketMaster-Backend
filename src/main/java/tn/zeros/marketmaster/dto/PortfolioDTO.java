package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Portfolio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDTO {
    private Long id;
    private Long userId;
    private List<HoldingDTO> holdings;
    private Map<LocalDateTime, Double> totalValue;
    private double cash;

    public static PortfolioDTO fromEntity(Portfolio portfolio) {
        return PortfolioDTO.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUser() != null ? portfolio.getUser().getId() : null)
                .holdings(portfolio.getHoldings() != null ?
                        portfolio.getHoldings().stream().map(HoldingDTO::fromEntity).toList()
                        : null)
                .totalValue(portfolio.getTotalValue())
                .cash(portfolio.getCash())
                .build();
    }

    public Portfolio toEntity() {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(this.id);
        portfolio.setTotalValue(this.totalValue);
        portfolio.setCash(this.cash);
        return portfolio;
    }
}
