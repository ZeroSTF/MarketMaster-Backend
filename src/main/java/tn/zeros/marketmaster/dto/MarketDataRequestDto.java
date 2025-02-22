package tn.zeros.marketmaster.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MarketDataRequestDto {
    private Long gameId;
    private String assetSymbol;
    private LocalDateTime lastPauseTimestamp;
}
