package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateEarned;
    private Integer currentCount;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    public void incrementCount() {
        this.currentCount++;
    }

    public boolean isAchieved(Achievement achievement) {
        return this.currentCount >= achievement.getRequiredCount();
    }

}
