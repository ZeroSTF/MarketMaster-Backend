package tn.zeros.marketmaster.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.MarketData;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketDataStreamDto {
    private LocalDateTime timestamp;
    private String assetSymbol;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;

    public MarketDataStreamDto(MarketData marketData) {
        this.timestamp = marketData.getTimestamp();
        this.assetSymbol = marketData.getAsset().getSymbol();
        this.open = marketData.getOpen();
        this.high = marketData.getHigh();
        this.low = marketData.getLow();
        this.close = marketData.getClose();
        this.volume = marketData.getVolume();
    }
}
