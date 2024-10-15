package tn.zeros.marketmaster.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameParticipation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "game_id")
    private Game game;

    private LocalDateTime joinTimestamp;

    @Column(columnDefinition = "BIGINT") // Being stored as nanoseconds at the moment
    private Duration totalPlayTime;

    private LocalDateTime lastResumeTimestamp;
    private LocalDateTime lastPauseTimestamp;

    private boolean isActive;
}
