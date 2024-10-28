package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.enums.GameStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime creationTimestamp;
    private LocalDate simulationStartDate;
    private LocalDate simulationEndDate;
    private GameStatus status;
    private String creatorUsername;

    // Static factory method to create GameDto from Game entity
    public static GameDto fromEntity(Game game) {
        GameDto dto = new GameDto();
        dto.setId(game.getId());
        dto.setTitle(game.getTitle());
        dto.setDescription(game.getDescription());
        dto.setCreationTimestamp(game.getCreationTimestamp());
        dto.setSimulationStartDate(game.getSimulationStartDate());
        dto.setSimulationEndDate(game.getSimulationEndDate());
        dto.setStatus(game.getStatus());
        dto.setCreatorUsername(game.getCreator().getUsername());
        return dto;
    }
}
