package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.zeros.marketmaster.entity.enums.OptionType;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Option implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    private OptionType type;
    private LocalDateTime dateEcheance;
    private double premium;
    private double strikePrice; // Prix d'exercice
    private double underlyingPrice;

}
