package tn.zeros.marketmaster.dto;

import lombok.Data;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.enums.GameStatus;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class NewGameDto {
    private String title;

    private String description;
    private LocalDateTime startTimestamp ;
    private LocalDateTime endTimestamp ;
    private Duration maxPlayTime;
    private String username ;
    private Long prize ;

    public  Game toEntity ()
    {
        return Game.builder().title(this.title).description(this.description)
                .startTimestamp(this.startTimestamp)
                .endTimestamp(this.endTimestamp)
                .maxPlayTime(this.maxPlayTime)
                .prize(this.prize).build();

    }



}
