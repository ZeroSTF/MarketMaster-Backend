package tn.zeros.marketmaster.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    private static final Logger logger = LoggerFactory.getLogger(GameController.class); // Logger instance

    private final GameService gameService;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    @PostMapping
    public ResponseEntity<?> createGame(@RequestBody Game game, @RequestParam String username) {

        // Fetch the user by username

        User creator = userRepository.findByUsername(username).orElseThrow(()-> new UserNotFoundException("user not found with email :"+username));



        // Call the gameService to create the game
        Game createdGame = gameService.createGame(game, creator);

        return ResponseEntity.ok(createdGame);
    }
}
