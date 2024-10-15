package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.zeros.marketmaster.entity.enums.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Transaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private Integer quantity;
    private double price;
    private LocalDateTime timestamp;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", asset=" + (asset != null ? asset.getSymbol() : "null") +
                ", type=" + type +
                ", quantity=" + quantity +
                ", price=" + price +
                ", timestamp=" + timestamp +
                ", portfolioId=" + (portfolio != null ? portfolio.getId() : "null") +
                '}';
    }
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
