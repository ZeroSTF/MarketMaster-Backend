package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LeaderboardEntryDto {
    private String username;
    private BigDecimal profit;
}