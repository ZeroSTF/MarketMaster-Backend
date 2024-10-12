package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.zeros.marketmaster.entity.enums.AchievementTier;
import tn.zeros.marketmaster.entity.enums.AchievementType;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private AchievementType type;
    @Enumerated(EnumType.STRING)
    private AchievementTier tier;
    private Integer requiredCount;
}
