package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlayerResultDto {
    private String username;
    private BigDecimal profit;
}
