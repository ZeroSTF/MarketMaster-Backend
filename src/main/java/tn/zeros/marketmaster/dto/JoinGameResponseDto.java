package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JoinGameResponseDto {
    private String message;
    private Long gameId;
    private String username;
}
