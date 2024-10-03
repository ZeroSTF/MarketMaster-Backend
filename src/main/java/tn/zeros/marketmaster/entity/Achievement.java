package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;  // Name of the achievement

    @Column(nullable = false)
    private String description;  // A brief description of the achievement

    @Column(nullable = false)
    private boolean isUnlocked;  // Whether the achievement has been unlocked by the user

    @Column(nullable = true)
    private String icon;  // Path or URL to the icon representing the achievement (optional)

    @Column(nullable = false)
    private Double reward;  // Reward for unlocking the achievement (could be points, cash, etc.)

    @Column(nullable = false)
    private Double progress;  // Progress towards unlocking the achievement (percentage or value)

    @Column(nullable = false)
    private String difficultyLevel;  // Difficulty of achieving this milestone (e.g., "easy", "medium", "hard")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Portfolio profile;  // Reference to the Profile that holds this achievement
}
