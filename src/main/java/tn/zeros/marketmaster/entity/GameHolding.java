package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameHolding implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_portfolio_id")
    private GamePortfolio gamePortfolio;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    private Integer quantity;

    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal averageCostBasis;

}
