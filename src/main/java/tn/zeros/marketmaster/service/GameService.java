package tn.zeros.marketmaster.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.*;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.GameParticipation;
import tn.zeros.marketmaster.entity.GamePortfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.enums.GameStatus;
import tn.zeros.marketmaster.repository.GameParticipationRepository;
import tn.zeros.marketmaster.repository.GamePortfolioRepository;
import tn.zeros.marketmaster.repository.GameRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository ;
    private final GamePortfolioRepository gamePortfolioRepository;
    private final GameParticipationRepository gameParticipationRepository;
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
    public JoinGameResponseDto joinGame(Long gameId, String username) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("No game found with ID: " + gameId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

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


        return new JoinGameResponseDto("Successfully joined the game and portfolio created", gameId, username);
    }
    @Transactional
    public List<GameDto> getCurrentGames() {
        User currentUser = userRepository.findByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Game> activeGames = gameRepository.findByStatus(GameStatus.ACTIVE);

        List<Game> participatedGames = gameParticipationRepository.findGamesByUser(currentUser);

        List<Game> nonParticipatedGames = activeGames.stream()
                .filter(game -> !participatedGames.contains(game))
                .collect(Collectors.toList());

        // Convert to DTOs
        return nonParticipatedGames.stream()
                .map(GameDto::fromEntity)
                .collect(Collectors.toList());
    }


    @Transactional
    public List<GameDto> getUpcomingGames() {
        List<Game> upcomingGames = gameRepository.findByStatus(GameStatus.UPCOMING);
        return upcomingGames.stream().map(GameDto::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public List<GameDto> getUserGames(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

        List<Game> userGames = gameParticipationRepository.findByUser(user)
                .stream().map(GameParticipation::getGame).collect(Collectors.toList());

        return userGames.stream().map(GameDto::fromEntity).collect(Collectors.toList());
    }
    @Transactional
    public List<LeaderboardEntryDto> getGameLeaderboard(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found"));

        List<GamePortfolio> portfolios = gamePortfolioRepository.findByGame(game);

        // Calculate profit for each portfolio and sort by profit
        return portfolios.stream()
                .map(portfolio -> {
                    BigDecimal profit = calculateProfit(portfolio);
                    return new LeaderboardEntryDto(portfolio.getUser().getUsername(), profit);
                })
                .sorted(Comparator.comparing(LeaderboardEntryDto::getProfit).reversed())
                .collect(Collectors.toList());
    }

    // Helper method to calculate profit based on holdings and transactions
    private BigDecimal calculateProfit(GamePortfolio portfolio) {
        // Calculate profit using holdings, initial cash, and other factors
        // This is a placeholder; actual profit calculation logic will depend on your requirements
        BigDecimal initialCash = BigDecimal.valueOf(10000); // Assuming an initial cash balance
        BigDecimal currentCash = BigDecimal.valueOf(portfolio.getCash());
        return currentCash.subtract(initialCash); // Replace with actual profit calculation
    }
    @Transactional
    public List<LeaderboardEntryDto> getGlobalLeaderboard() {
        // Retrieve all unique users
        List<User> users = gameParticipationRepository.findAllUsers();

        return users.stream()
                .map(user -> {
                    BigDecimal totalProfit = calculateTotalProfit(user);
                    return new LeaderboardEntryDto(user.getUsername(), totalProfit);
                })
                .sorted(Comparator.comparing(LeaderboardEntryDto::getProfit).reversed())
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalProfit(User user) {
        List<GamePortfolio> portfolios = gamePortfolioRepository.findByUser(user);
        BigDecimal totalProfit = portfolios.stream()
                .map(this::calculateProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalProfit;
    }

    @Transactional
    public PlayerPerformanceDto getPlayerPerformance(String username) {
        List<GameParticipation> participations = gameParticipationRepository.findByUserUsername(username);

        int gamesPlayed = participations.size();
        int gamesWon = calculateGamesWon(participations);
        BigDecimal totalProfit = calculateTotalProfit(username);
        BigDecimal averageProfit = gamesPlayed > 0 ? totalProfit.divide(BigDecimal.valueOf(gamesPlayed*10000), RoundingMode.HALF_UP) : BigDecimal.ZERO;
        double winRate = gamesPlayed > 0 ? (double) gamesWon / gamesPlayed * 100 : 0;

        return new PlayerPerformanceDto(username, gamesPlayed, gamesWon, winRate, totalProfit, averageProfit);
    }

    private BigDecimal calculateTotalProfit(String username) {
        List<GamePortfolio> portfolios = gamePortfolioRepository.findByUserUsername(username);
        return portfolios.stream()
                .map(this::calculateProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    private int calculateGamesWon(List<GameParticipation> participations) {
        return (int) participations.stream()
                .filter(participation -> participation.getGame().getStatus() == GameStatus.COMPLETED)
                .filter(participation -> didPlayerWin(participation))
                .count();
    }

    private boolean didPlayerWin(GameParticipation participation) {
        GamePortfolio portfolio = gamePortfolioRepository.findByUserAndGame(participation.getUser(), participation.getGame())
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found"));

        BigDecimal profit = calculateProfit(portfolio);
        return profit.compareTo(BigDecimal.ZERO) > 0;
    }

    /*@Transactional
    public GamePerformanceDto getGamePerformance(Long gameId, String username) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found"));

        GamePortfolio portfolio = gamePortfolioRepository.findByUserUsernameAndGameId(username, gameId)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found for user in this game"));

        BigDecimal profit = calculateProfit(portfolio);
        BigDecimal holdingsValue = calculateHoldingsValue(portfolio);
        BigDecimal totalValue = holdingsValue.add(BigDecimal.valueOf(portfolio.getCash()));

        int rank = calculateRank(gameId, portfolio);

        return new GamePerformanceDto(username, profit, holdingsValue, portfolio.getCash(), totalValue, rank);
    }
*/

    /*private BigDecimal calculateHoldingsValue(GamePortfolio portfolio) {
        return portfolio.getGameHoldings().stream()
                .map(holding -> holding.getAsset().getCurrentPrice().multiply(BigDecimal.valueOf(holding.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }*/

    private int calculateRank(Long gameId, GamePortfolio userPortfolio) {
        List<GamePortfolio> portfolios = gamePortfolioRepository.findByGameId(gameId);

        List<GamePortfolio> rankedPortfolios = portfolios.stream()
                .sorted(Comparator.comparing(this::calculateProfit).reversed())
                .collect(Collectors.toList());

        return rankedPortfolios.indexOf(userPortfolio) + 1;
    }
}
