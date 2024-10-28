package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlayerPerformanceDto {
    private String username;
    private int gamesPlayed;
    private int gamesWon;
    private double winRate;
    private BigDecimal totalProfit;
    private BigDecimal averageProfit;
}
