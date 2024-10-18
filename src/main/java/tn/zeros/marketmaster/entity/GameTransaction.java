package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.zeros.marketmaster.entity.enums.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameTransaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_portfolio_id")
    private GamePortfolio gamePortfolio;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    private TransactionType type;
    private Integer quantity;
    private Double price;
    private LocalDateTime simulationTimestamp;
    private LocalDateTime realTimestamp;

    @PrePersist
    protected void onCreate() {
        realTimestamp = LocalDateTime.now();
    }
}
