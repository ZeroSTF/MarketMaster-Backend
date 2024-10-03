package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;  // The unique ticker symbol for the asset (e.g., AAPL for Apple)

    @Column(nullable = false)
    private String name;  // Full name of the asset (e.g., "Apple Inc.")

    @Column(nullable = false)
    private Double currentPrice;  // The current market price of the asset

    @Column(nullable = true)
    private String priceHistory;  // Historical price data for the asset (could be a JSON or CSV format)

    @Column(nullable = false)
    private Double volatility;  // A measure of how much the asset's price fluctuates

    @Column(nullable = false)
    private String assetType;  // Type of asset (e.g., stock, bond, cryptocurrency)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Portfolio profile;  // Reference to the Profile that holds this asset
}
