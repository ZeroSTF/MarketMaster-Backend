package tn.zeros.marketmaster.dto;

import lombok.Data;
import tn.zeros.marketmaster.entity.Game;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class NewGameResponseDto {
    private Long id;
    private String description;
    private LocalDateTime startTimestamp;
    private LocalDateTime endTimestamp;
    private Duration maxPlayTime;
    private String creatorUsername;
    private LocalDate simulationStartDate;
    private Long prize ;


    // Factory method to convert Game entity to GameResponseDto
    public static NewGameResponseDto fromEntity(Game game) {
        NewGameResponseDto dto = new NewGameResponseDto();
        dto.setId(game.getId());
        dto.setDescription(game.getDescription());
        dto.setStartTimestamp(game.getStartTimestamp());
        dto.setEndTimestamp(game.getEndTimestamp());
        dto.setMaxPlayTime(game.getMaxPlayTime());
        dto.setCreatorUsername(game.getCreator().getUsername());
        dto.setSimulationStartDate(game.getSimulationStartDate());
        dto.setPrize(game.getPrize());
        return dto;
    }
}

