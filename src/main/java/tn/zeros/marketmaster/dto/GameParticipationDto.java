package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.GameParticipation;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameParticipationDto {
    private Long id; // Participation ID
    private Long gameId; // Game the user participated in
    private String username; // Username of the participant
    private LocalDateTime joinTimestamp; // When the user joined the game
    private LocalDateTime lastResumeTimestamp; // Last time the user resumed the game
    private LocalDateTime lastPauseTimestamp; // Last time the user paused the game
    private Duration totalPlayTime; // Total playtime accumulated
    private float timeAccelerationFactor; // Speed of simulation (e.g., 1x, 2x, etc.)
    private boolean isActive; // Whether the user is actively participating

    public GameParticipationDto(GameParticipation participation) {
        this.id = participation.getId();
        this.gameId = participation.getGame().getId();
        this.username = participation.getUser().getUsername();
        this.joinTimestamp = participation.getJoinTimestamp();
        this.lastResumeTimestamp = participation.getLastResumeTimestamp();
        this.lastPauseTimestamp = participation.getLastPauseTimestamp();
        this.totalPlayTime = participation.getTotalPlayTime();
        this.timeAccelerationFactor = participation.getTimeAccelerationFactor();
        this.isActive = participation.isActive();
    }
}
