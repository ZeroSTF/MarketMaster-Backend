package tn.zeros.marketmaster.dto;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import tn.zeros.marketmaster.entity.MarketData;

import java.time.LocalDateTime;
@JsonSerialize
@Data
public class MarketDataStreamDto {
    private Long id;
    private LocalDateTime timestamp;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
    private String assetSymbol;

    public MarketDataStreamDto(MarketData marketData) {
        this.id = marketData.getId();
        this.timestamp = marketData.getTimestamp();
        this.open = marketData.getOpen();
        this.high = marketData.getHigh();
        this.low = marketData.getLow();
        this.close = marketData.getClose();
        this.volume = marketData.getVolume();
        this.assetSymbol = marketData.getAsset().getSymbol();
    }

}

