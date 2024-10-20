package tn.zeros.marketmaster.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.JoinGameDto;
import tn.zeros.marketmaster.dto.JoinGameResponseDto;
import tn.zeros.marketmaster.dto.NewGameDto;
import tn.zeros.marketmaster.dto.NewGameResponseDto;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.GameParticipation;
import tn.zeros.marketmaster.entity.GamePortfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.enums.GameStatus;
import tn.zeros.marketmaster.repository.GameParticipationRepository;
import tn.zeros.marketmaster.repository.GamePortfolioRepository;
import tn.zeros.marketmaster.repository.GameRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
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
    private final UserService userService ;
    private final UserRepository userRepository;

    // Utility method to generate random date in the last 20 years
    private LocalDate getRandomDateWithinLast20Years() {
        LocalDate now = LocalDate.now();
        LocalDate twentyYearsAgo = now.minusYears(20);

        Random random = new Random();
        long daysBetween = twentyYearsAgo.until(now, java.time.temporal.ChronoUnit.DAYS);
        long randomDays = random.nextInt((int) daysBetween + 1);

        return twentyYearsAgo.plusDays(randomDays);
    }

    @Transactional
    public NewGameResponseDto createGame(NewGameDto gameDto) {
        User creator = userRepository.findByUsername(gameDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + gameDto.getUsername()));

        LocalDate simulationStartDate = getRandomDateWithinLast20Years();

        LocalDate simulationEndDate = simulationStartDate.plusDays(3);

        Game game = gameDto.toEntity();
        game.setSimulationStartDate(simulationStartDate);
        game.setSimulationEndDate(simulationEndDate);
        game.setCreator(creator);
        game.setCreationTimestamp(LocalDateTime.now());
        game.setStatus(GameStatus.ACTIVE);


        Game createdGame = gameRepository.save(game);


        GamePortfolio portfolio = new GamePortfolio();
        portfolio.setGame(createdGame);
        portfolio.setUser(creator);
        portfolio.setCash(10000);
        gamePortfolioRepository.save(portfolio);

        GameParticipation participation = new GameParticipation();
        participation.setGame(createdGame);
        participation.setJoinTimestamp(LocalDateTime.now());
        participation.setActive(true);
        participation.setTotalPlayTime(Duration.ZERO);
        participation.setUser(creator);
        gameParticipationRepository.save(participation);

        return NewGameResponseDto.fromEntity(createdGame);
    }
    @Transactional
    public JoinGameResponseDto joinGame(Long gameId, JoinGameDto joinGameDto) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("No game found with ID: " + gameId));

        User user = userRepository.findByUsername(joinGameDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + joinGameDto.getUsername()));

        boolean isAlreadyParticipating = gameParticipationRepository.existsByGameAndUser(game, user);
        if (isAlreadyParticipating) {
            throw new IllegalStateException("User already joined this game.");
        }

        GameParticipation participation = new GameParticipation();
        participation.setGame(game);
        participation.setJoinTimestamp(LocalDateTime.now());
        participation.setActive(true);
        participation.setTotalPlayTime(Duration.ZERO); // Initial play time
        participation.setUser(user);
        gameParticipationRepository.save(participation);


        GamePortfolio portfolio = new GamePortfolio();
        portfolio.setGame(game);
        portfolio.setUser(user);
        portfolio.setCash(10000);
        gamePortfolioRepository.save(portfolio);


        return new JoinGameResponseDto("Successfully joined the game and portfolio created", gameId, joinGameDto.getUsername());
    }
}
