package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverviewDTO {
    private double totalValue;
    private double totalValuePercentage;
    private BigDecimal dailyChange;
    private double cash;
    private double cashPercentage;
    private double annualReturn;
    private int holdingNumber;
}
