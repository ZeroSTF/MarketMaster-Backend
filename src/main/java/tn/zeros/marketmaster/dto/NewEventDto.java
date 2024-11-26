package tn.zeros.marketmaster.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tn.zeros.marketmaster.entity.Game;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewEventDto extends NewGameDto {
    private LocalDate simulationStartDate; // Field for admin to specify a specific start date

    @Override
    public Game toEntity() {
        Game game = super.toEntity();
        game.setSimulationStartDate(this.simulationStartDate); // Set the admin-specified start date
        return game;
    }
}
