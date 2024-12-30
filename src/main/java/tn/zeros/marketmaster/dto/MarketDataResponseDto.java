package tn.zeros.marketmaster.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MarketDataResponseDto {
    private List<MarketDataStreamDto> pastMarketData;    // Simulated data
    private List<MarketDataStreamDto> upcomingMarketData; // Next 50 lines
}
