package tn.zeros.marketmaster.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import tn.zeros.marketmaster.entity.MarketData;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MarketDataStreamDto implements Serializable {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
    private String assetSymbol;

    public MarketDataStreamDto(MarketData marketData) {
        this.id = marketData.getId();
        this.timestamp = marketData.getTimestamp(); // Keep as LocalDateTime
        this.open = marketData.getOpen();
        this.high = marketData.getHigh();
        this.low = marketData.getLow();
        this.close = marketData.getClose();
        this.volume = marketData.getVolume();
        this.assetSymbol = marketData.getAsset().getSymbol();
    }
}