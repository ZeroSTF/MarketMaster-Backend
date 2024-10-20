package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.GameParticipation;
import tn.zeros.marketmaster.entity.GamePortfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.repository.GameParticipationRepository;
import tn.zeros.marketmaster.repository.GamePortfolioRepository;
import tn.zeros.marketmaster.repository.GameRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository ;
    private final GamePortfolioRepository gamePortfolioRepository;
    private final GameParticipationRepository gameParticipationRepository;

    // Utility method to generate random date in the last 20 years
    private LocalDateTime getRandomDateWithinLast20Years() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twentyYearsAgo = now.minusYears(20);

        Random random = new Random();
        long daysBetween = twentyYearsAgo.until(now, java.time.temporal.ChronoUnit.DAYS);
        long randomDays = random.nextInt((int) daysBetween + 1);

        return twentyYearsAgo.plusDays(randomDays);
    }

    public Game createGame(Game game, User creator) {
        // 1. Generate a random date in the last 20 years
        LocalDateTime startDate = getRandomDateWithinLast20Years();

        // 2. Calculate the end date (3 hours later)
        LocalDateTime endDate = startDate.plusHours(3);

        // 3. Set the start and end timestamps for the game simulation
        game.setSimulationStartDate(startDate.toLocalDate());
        game.setSimulationEndDate(endDate.toLocalDate());
        game.setCreationTimestamp(LocalDateTime.now());
        game.setCreator(creator);

        // 4. Save the game
        Game savedGame = gameRepository.save(game);

        // 5. Create GamePortfolio for the user
        GamePortfolio gamePortfolio = GamePortfolio.builder()
                .game(savedGame)
                .user(creator)
                .cash(10000.0) // Initial cash for the player
                .build();

        // 6. Save the GamePortfolio
        gamePortfolioRepository.save(gamePortfolio);

        // 7. Create GameParticipation for the user
        GameParticipation gameParticipation = GameParticipation.builder()
                .game(savedGame)
                .user(creator)
                .joinTimestamp(LocalDateTime.now())
                .timeAccelerationFactor(1.0f)  // Default time acceleration factor
                .isActive(true)
                .build();

        // 8. Save the GameParticipation
        gameParticipationRepository.save(gameParticipation);

        return savedGame;
    }
}
