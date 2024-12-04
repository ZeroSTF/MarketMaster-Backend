package tn.zeros.marketmaster.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class EndGameResponseDto {
    private Long gameId;
    private String gameName;
    private List<PlayerResultDto> playerResults;
}
