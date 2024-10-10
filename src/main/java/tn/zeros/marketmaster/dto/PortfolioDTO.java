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
    private double annualReturn;
    private Long currentRank;
    private double cash;
    private String currency;
    private double changeOfToday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Convert Entity to DTO
    public static PortfolioDTO fromEntity(Portfolio portfolio) {
        return PortfolioDTO.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUser().getId())
                .holdings(portfolio.getHoldings().stream().map(HoldingDTO::fromEntity).toList())
                .totalValue(portfolio.getTotalValue())
                .annualReturn(portfolio.getAnnualReturn())
                .currentRank(portfolio.getCurrentRank())
                .cash(portfolio.getCash())
                .currency(portfolio.getCurrency())
                .changeOfToday(portfolio.getChangeOfToday())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    // Convert DTO to Entity
    public Portfolio toEntity() {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(this.id);
        portfolio.setTotalValue(this.totalValue);
        portfolio.setAnnualReturn(this.annualReturn);
        portfolio.setCurrentRank(this.currentRank);
        portfolio.setCash(this.cash);
        portfolio.setCurrency(this.currency);
        portfolio.setChangeOfToday(this.changeOfToday);
        portfolio.setCreatedAt(this.createdAt);
        portfolio.setUpdatedAt(this.updatedAt);
        // Note: User and Holdings need to be set separately
        return portfolio;
    }
}
