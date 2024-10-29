package tn.zeros.marketmaster.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.*;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.UserRepository;
import tn.zeros.marketmaster.service.GameService;
import tn.zeros.marketmaster.service.JwtTokenService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
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
            @RequestBody String username) {
        JoinGameResponseDto gameResponse = gameService.joinGame(gameId, username);
        return ResponseEntity.ok(gameResponse);
    }
    @GetMapping("/active")
    public ResponseEntity<List<GameDto>> getCurrentGames() {
        List<GameDto> activeGames = gameService.getCurrentGames();
        return ResponseEntity.ok(activeGames);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<GameDto>> getUpcomingGames() {
        List<GameDto> upcomingGames = gameService.getUpcomingGames();
        return ResponseEntity.ok(upcomingGames);
    }


    @GetMapping("/user/{username}")
    public ResponseEntity<List<GameDto>> getUserGames(@PathVariable String username) {
        List<GameDto> userGames = gameService.getUserGames(username);
        return ResponseEntity.ok(userGames);
    }

    @GetMapping("/leaderboard/{gameId}")
    public ResponseEntity<List<LeaderboardEntryDto>> getGameLeaderboard(@PathVariable Long gameId) {
        List<LeaderboardEntryDto> leaderboard = gameService.getGameLeaderboard(gameId);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/globalleaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getGlobalLeaderboard() {
        List<LeaderboardEntryDto> leaderboard = gameService.getGlobalLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/globalperformance/{username}")
    public ResponseEntity<PlayerPerformanceDto> getPlayerPerformance(@PathVariable String username) {
        PlayerPerformanceDto performanceDto = gameService.getPlayerPerformance(username);
        return ResponseEntity.ok(performanceDto);
    }


}
