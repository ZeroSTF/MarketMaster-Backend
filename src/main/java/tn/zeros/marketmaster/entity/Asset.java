package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.zeros.marketmaster.entity.enums.AssetType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;


    @Column(nullable = false)
    private String name;



    @OneToMany(mappedBy = "asset", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MarketData> marketData = new HashSet<>();

}
