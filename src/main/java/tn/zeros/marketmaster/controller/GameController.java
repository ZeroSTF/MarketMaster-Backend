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
import tn.zeros.marketmaster.service.GameResultsService;
import tn.zeros.marketmaster.service.GameService;
import tn.zeros.marketmaster.service.GameTransactionService;
import tn.zeros.marketmaster.service.JwtTokenService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/games")
public class GameController {


    private final GameService gameService;
    private final GameTransactionService gameTransactionService;
    private final GameResultsService gameResultsService ;


    @PostMapping("/create")
    public ResponseEntity<?> createGame(@RequestBody NewGameDto game) {

        NewGameResponseDto createdGame = gameService.createGame(game);

        return ResponseEntity.ok(createdGame);
    }
    @PostMapping("/create/event")
    public ResponseEntity<?> createEvent(@RequestBody NewEventDto game) {

        NewGameResponseDto createdGame = gameService.createGameWithSpecifiedDate(game);

        return ResponseEntity.ok(createdGame);
    }

    @PostMapping("/{gameId}/join/{username}")
    public ResponseEntity<JoinGameResponseDto> joinGame(
            @PathVariable Long gameId,
            @PathVariable String username) {
        System.out.println("Joining game with ID: " + gameId + " and username: " + username);
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

    @GetMapping("/{gameId}/state")
    public ResponseEntity<GameStateDto> getGameState(
            @PathVariable Long gameId,
            @RequestParam String username) {
        GameStateDto gameState = gameService.getGameState(gameId, username);
        return ResponseEntity.ok(gameState);
    }

    @PostMapping("/market-data")
    public ResponseEntity<MarketDataResponseDto> getMarketData(@RequestBody MarketDataRequestDto request) {
        return ResponseEntity.ok(gameService.getMarketData(request));
    }
    @PutMapping("/update-timestamp")
    public ResponseEntity<Void> updateTimestamp(@RequestBody UpdateTimestampRequest request) {
        boolean success = gameService.updateGameParticipationTimestamp(request.getGameParticipationId(), request.getLastPauseTimestamp());

        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(400).build();
        }
    }
    @GetMapping("/news/{gameId}")
    public ResponseEntity<List<NewsArticleResponseDto>> getNewsByGame(@PathVariable Long gameId) {
        List<NewsArticleResponseDto> newsArticles = gameService.getNewsByGameId(gameId);
        return ResponseEntity.ok(newsArticles);
    }
    @PostMapping("/transaction")
    public ResponseEntity<Map<String, String>> processTransaction(@RequestBody GameTransactionDto transactionRequest) {
        try {
            gameTransactionService.processTransaction(transactionRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Transaction processed successfully");
            return ResponseEntity.ok(response); // Return JSON response
        } catch (IllegalArgumentException | IllegalStateException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Transaction failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response); // Return JSON response
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response); // Return JSON response
        }
    }

    @GetMapping("/{gameId}/results")
    public GameResultsDto getGameResults(
            @PathVariable Long gameId,
            @RequestParam String username // Use username as a query parameter
    ) {
        return gameResultsService.getGameResults(gameId, username);
    }

}
