package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Portfolio implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Holding> holdings = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "portfolio_total_value", joinColumns = @JoinColumn(name = "portfolio_id"))
    @MapKeyColumn(name = "date")
    @Column(name = "value")
    @OrderBy("date ASC")
    private Map<LocalDateTime, Double> totalValue= new HashMap<>();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new LinkedHashSet<>();

    private double cash;
}
