package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.zeros.marketmaster.entity.enums.OrderStatus;
import tn.zeros.marketmaster.entity.enums.TransactionType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class LimitOrder implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private Integer quantity;
    private Double limitPrice;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private LocalDateTime creationTimestamp;
    private LocalDateTime executionTimestamp;

}
