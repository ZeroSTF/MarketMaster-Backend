package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Holding> holdings;

    @ElementCollection
    @CollectionTable(name = "portfolio_total_value", joinColumns = @JoinColumn(name = "portfolio_id"))
    @MapKeyColumn(name = "date") // Column for the date key in the map
    @Column(name = "value") // Column for the map values (Double)
    private Map<LocalDateTime, Double> totalValue;

    @Min(0)
    private double annualReturn;
    private Long currentRank;

    private double cash;
    private String currency; // Optional for tracking portfolio currency (e.g., USD, EUR)
    private double changeOfToday;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
