package tn.zeros.marketmaster.dto;

import lombok.Data;

@Data
public class MarketDataRequestDto {
    private Long gameId;
    private String assetSymbol;
    private int updateRate; // Update rate in seconds
}
