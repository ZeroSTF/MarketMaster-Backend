package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketData implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    private Double open;  // Opening price
    private Double high;  // Highest price during the time period
    private Double low;   // Lowest price during the time period
    private Double close; // Closing price at the end of the time period
    private Long volume;  // Total volume during the time period

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;
}
