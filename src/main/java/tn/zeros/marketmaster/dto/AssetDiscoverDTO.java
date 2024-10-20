package tn.zeros.marketmaster.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AssetDiscoverDTO {
    private String symbol;
    private double open;
    private double high;
    private double low;
    private double price;
    private long volume;
    private String latestTradingDay;
    private double previousClose;
    private double change;
    private String changePercent;

}

