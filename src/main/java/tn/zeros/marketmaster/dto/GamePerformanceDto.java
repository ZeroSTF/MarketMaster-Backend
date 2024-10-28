package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class GamePerformanceDto {
    private String username;
    private BigDecimal profit;
    private BigDecimal holdingsValue;
    private double cash;
    private BigDecimal totalValue;
    private int rank;
}
