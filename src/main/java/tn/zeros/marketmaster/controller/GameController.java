package tn.zeros.marketmaster.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.JoinGameDto;
import tn.zeros.marketmaster.dto.JoinGameResponseDto;
import tn.zeros.marketmaster.dto.NewGameDto;
import tn.zeros.marketmaster.dto.NewGameResponseDto;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.UserRepository;
import tn.zeros.marketmaster.service.GameService;
import tn.zeros.marketmaster.service.JwtTokenService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/games")
public class GameController {


    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity<?> createGame(@RequestBody NewGameDto game) {

        NewGameResponseDto createdGame = gameService.createGame(game);

        return ResponseEntity.ok(createdGame);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<JoinGameResponseDto> joinGame(
            @PathVariable Long gameId,
            @RequestBody JoinGameDto joinGameDto) {
        JoinGameResponseDto gameResponse = gameService.joinGame(gameId, joinGameDto);
        return ResponseEntity.ok(gameResponse);
    }
}
