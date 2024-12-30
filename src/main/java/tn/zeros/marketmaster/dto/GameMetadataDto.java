package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.enums.GameStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GameMetadataDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime creationTimestamp;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private GameStatus status;

    public GameMetadataDto(Game game) {
        this.id = game.getId();
        this.title = game.getTitle();
        this.description = game.getDescription();
        this.creationTimestamp = game.getCreationTimestamp();
        this.startTimestamp = game.getStartTimestamp();
        this.endTimestamp = game.getEndTimestamp();
        this.status = game.getStatus();
    }
}

