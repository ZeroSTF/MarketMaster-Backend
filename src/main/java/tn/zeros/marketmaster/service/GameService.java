package tn.zeros.marketmaster.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tn.zeros.marketmaster.dto.*;
import tn.zeros.marketmaster.entity.*;
import tn.zeros.marketmaster.entity.enums.GameStatus;
import tn.zeros.marketmaster.exception.GameNotFoundException;
import tn.zeros.marketmaster.repository.*;

import javax.sound.sampled.Port;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository ;
    private final GamePortfolioRepository gamePortfolioRepository;
    private final GameParticipationRepository gameParticipationRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository ;
    private final MarketDataRepository marketDataRepository ;
    private final RestTemplate restTemplate;
    private final PortfolioRepository portfolioRepository;
    private final NewsArticleRepository newsArticleRepository ;
    private final GameHoldingRespository gameHoldingRespository;

    private static final String FLASK_API_URL_HISTORY = "http://localhost:5000/api/assets/history";
    private static final String FLASK_API_URL_NEWS = "http://localhost:5000/api/assets/news";

    private static final Logger logger = LoggerFactory.getLogger(GameService.class);  // Initialize the logger
    public GameStateDto getGameState(Long gameId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + gameId));

        GameParticipation participation = gameParticipationRepository
                .findByGameAndUser(game, user)
                .orElseThrow(() -> new EntityNotFoundException("Participation not found for user in this game"));

        GamePortfolio portfolio = gamePortfolioRepository.findByUserAndGame(user, game)
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found for user in this game"));

        // Fetch the latest MarketData timestamp for the game
        LocalDateTime lastMarketDataTimestamp = marketDataRepository
                .findLastMarketDataTimestampByGameId(gameId)
                .orElse(null); // Use null or a default value if no MarketData exists

        return GameStateDto.builder()
                .gameMetadata(new GameMetadataDto(game, lastMarketDataTimestamp)) // Include the lastMarketDataTimestamp
                .gameParticipation(new GameParticipationDto(participation))
                .gamePortfolio(new GamePortfolioDto(portfolio))
                .build();
    }

    public MarketDataResponseDto getMarketData(MarketDataRequestDto request) {
        // Fetch Game
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new EntityNotFoundException("Game not found with ID: " + request.getGameId()));

        // Fetch Asset
        Asset asset = assetRepository.findBySymbol(request.getAssetSymbol());


        // Fetch Past Market Data
        List<MarketData> pastMarketData = marketDataRepository
                .findByGameAndAssetAndTimestampBeforeOrderByTimestampAsc(game, asset, request.getLastPauseTimestamp());

        // Fetch Upcoming Market Data (next 50)
        List<MarketData> upcomingMarketData = marketDataRepository
                .findTop50ByGameAndAssetAndTimestampAfterOrderByTimestampAsc(game, asset, request.getLastPauseTimestamp());

        // Map to DTO
        return MarketDataResponseDto.builder()
                .pastMarketData(pastMarketData.stream().map(MarketDataStreamDto::new).collect(Collectors.toList()))
                .upcomingMarketData(upcomingMarketData.stream().map(MarketDataStreamDto::new).collect(Collectors.toList()))
                .build();
    }


    private LocalDate getRandomDateWithinLast20Years() {
        LocalDate now = LocalDate.now();
        LocalDate twentyYearsAgo = now.minusDays(60);

        Random random = new Random();
        long daysBetween = twentyYearsAgo.until(now, java.time.temporal.ChronoUnit.DAYS);
        long randomDays = random.nextInt((int) daysBetween + 2);

        return twentyYearsAgo.plusDays(randomDays);
    }

    @Transactional
    public NewGameResponseDto createGame(NewGameDto gameDto) {
        // Fetch the game creator
        User creator = userRepository.findByUsername(gameDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + gameDto.getUsername()));

        // Generate a random simulation date range
        LocalDate simulationStartDate = getRandomDateWithinLast20Years();
        LocalDate simulationEndDate = simulationStartDate.plusDays(10);

        // Create the game entity and set its initial properties
        Game game = gameDto.toEntity();
        game.setSimulationStartDate(simulationStartDate);
        game.setCreator(creator);
        game.setCreationTimestamp(LocalDateTime.now());
        game.setStatus(GameStatus.ACTIVE);
        game.setSimulationStartDate(simulationStartDate);


        // Save the game to get its ID
        Game createdGame = gameRepository.save(game);

        // Fetch market data and news articles for the specified date range
        List<MarketData> marketDataList = fetchMarketDataFromFlask(simulationStartDate, simulationEndDate);
        List<NewsArticle> newsArticles = fetchNewsFromFlask(simulationStartDate, simulationEndDate);

        // Set the associated game for each MarketData entry
        for (MarketData marketData : marketDataList) {
            marketData.setGame(createdGame);
        }

        // Save all market data entries
        if (!marketDataList.isEmpty()) {
            marketDataRepository.saveAll(marketDataList);
        } else {
            System.out.println("No market data fetched to save.");
        }

        // Set the associated game for each NewsArticle entry
        for (NewsArticle newsArticle : newsArticles) {
            newsArticle.setGame(createdGame);
        }

        if (!newsArticles.isEmpty()) {
            newsArticleRepository.saveAll(newsArticles);
        } else {
            System.out.println("No news articles fetched to save.");
        }

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
        participation.setLastPauseTimestamp(game.getSimulationStartDate().atTime(12,0));
        gameParticipationRepository.save(participation);

        // Return the response DTO
        return NewGameResponseDto.fromEntity(createdGame);
    }




    private List<MarketData> fetchMarketDataFromFlask(LocalDate startDate, LocalDate endDate) {
        String start = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String end = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Fetch asset symbols
        List<String> symbols = assetRepository.findAll().stream()
                .map(Asset::getSymbol)
                .collect(Collectors.toList());

        // Build URI with symbols as query parameters
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(FLASK_API_URL_HISTORY)
                .queryParam("start", start)
                .queryParam("end", end);

        for (String symbol : symbols) {
            uriBuilder.queryParam("symbols", symbol);
        }

        try {
            // Fetch data from Flask API
            ResponseEntity<Map<String, List<MarketDataDto>>> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, List<MarketDataDto>>>() {}
            );

            Map<String, List<MarketDataDto>> dataMap = response.getBody();
            if (dataMap == null) {
                return List.of();
            }

            List<MarketData> marketDataList = new ArrayList<>();
            for (Map.Entry<String, List<MarketDataDto>> entry : dataMap.entrySet()) {
                String symbol = entry.getKey();
                Asset asset = assetRepository.findBySymbol(symbol);


                for (MarketDataDto dto : entry.getValue()) {
                    MarketData marketData = new MarketData();
                    marketData.setTimestamp(dto.getDate().toLocalDateTime());
                    marketData.setOpen(dto.getOpen());
                    marketData.setHigh(dto.getHigh());
                    marketData.setLow(dto.getLow());
                    marketData.setClose(dto.getClose());
                    marketData.setVolume(dto.getVolume());
                    marketData.setAsset(asset);
                    marketDataList.add(marketData);
                }
            }

            return marketDataList;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch market data from Flask API", e);
        }
    }

    private List<NewsArticle> fetchNewsFromFlask(LocalDate startDate, LocalDate endDate) {
        String start = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String end = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Fetch asset symbols
        List<String> symbols = assetRepository.findAll().stream()
                .map(Asset::getSymbol)
                .collect(Collectors.toList());

        // Build URI with symbols as query parameters
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(FLASK_API_URL_NEWS)
                .queryParam("start", start)
                .queryParam("end", end);

        for (String symbol : symbols) {
            uriBuilder.queryParam("symbols", symbol);
        }

        try {
            // Fetch data from Flask API
            ResponseEntity<Map<String, List<NewsArticleDto>>> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, List<NewsArticleDto>>>() {}
            );

            Map<String, List<NewsArticleDto>> dataMap = response.getBody();
            if (dataMap == null) {
                return List.of();
            }

            List<NewsArticle> newsArticles = new ArrayList<>();
            for (Map.Entry<String, List<NewsArticleDto>> entry : dataMap.entrySet()) {
                String symbol = entry.getKey();
                Asset asset = assetRepository.findBySymbol(symbol);

                for (NewsArticleDto dto : entry.getValue()) {
                    NewsArticle newsArticle = new NewsArticle();
                    newsArticle.setCategory(dto.getCategory());
                    newsArticle.setHeadline(dto.getHeadline());
                    newsArticle.setSource(dto.getSource());
                    newsArticle.setRelated(dto.getRelated());
                    newsArticle.setUrl(dto.getUrl());
                    newsArticle.setImage(dto.getImage());
                    newsArticle.setPublishedDate(dto.getPublishedDate());
                    newsArticles.add(newsArticle);
                }
            }

            return newsArticles;

        } catch (Exception e) {
            logger.error("Failed to fetch news articles from Flask API", e);
            throw new RuntimeException("Failed to fetch news articles from Flask API", e);
        }
    }





    @Transactional
    public NewGameResponseDto createGameWithSpecifiedDate(NewEventDto gameDto) {
        User creator = userRepository.findByUsername(gameDto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + gameDto.getUsername()));

        LocalDate simulationStartDate = gameDto.getSimulationStartDate(); // Use the date specified by admin

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
                .orElseThrow(() -> new GameNotFoundException("No game found with ID: " + gameId));

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
        participation.setLastPauseTimestamp(game.getSimulationStartDate().atTime(12,0));
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
        BigDecimal initialCash = BigDecimal.valueOf(10000); // Replace with actual initial cash value
        BigDecimal currentCash = BigDecimal.valueOf(portfolio.getCash()).add(totalHoldingsFinalValue(portfolio));

        // Calculate profit and round to 2 decimal places
        return currentCash.subtract(initialCash).setScale(2, RoundingMode.HALF_UP);
    }



    @Transactional
    public BigDecimal totalHoldingsFinalValue(GamePortfolio portfolio) {
        logger.info("Calculating total holdings final value for portfolio: {}", portfolio.getId());

        Set<GameHolding> holdings = portfolio.getGameHoldings();
        logger.info("Retrieved {} holdings for portfolio: {}", holdings.size(), portfolio.getId());
        User user = portfolio.getUser();
        Game game = portfolio.getGame();

        BigDecimal totalFinalValue = BigDecimal.ZERO;
GameParticipation participation = gameParticipationRepository.findByGameAndUser(game , user).orElseThrow(()->new GameNotFoundException("game not found "));
        for (GameHolding holding : holdings) {
            Asset asset = holding.getAsset();
            Integer quantity = holding.getQuantity();

            logger.info("Processing holding: Asset ID = {}, Quantity = {}", asset.getId(), quantity);

            // Fetch the latest market data for the asset in the current game
            MarketData latestMarketData = marketDataRepository.findTopByAssetAndGameAndTimestampBeforeOrderByTimestampDesc(asset, portfolio.getGame(),participation.getLastPauseTimestamp()).orElseThrow(() -> new GameNotFoundException("No market data found for asset before timestamp"));
            ;

            if (latestMarketData != null) {
                BigDecimal latestPrice = BigDecimal.valueOf(latestMarketData.getClose());
                BigDecimal holdingValue = latestPrice.multiply(BigDecimal.valueOf(quantity));
                totalFinalValue = totalFinalValue.add(holdingValue);

                logger.info("Latest market price for asset {}: {}. Holding value: {}", asset.getId(), latestPrice, holdingValue);
            } else {
                logger.info("No market data found for asset ID {} in game ID {}", asset.getId(), portfolio.getGame().getId());
            }
        }

        logger.info("Total final value for portfolio {}: {}", portfolio.getId(), totalFinalValue);
        return totalFinalValue;
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

        List<GameParticipation> participations;
        try {
            participations = gameParticipationRepository.findByUserUsername(username);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw e; // Re-throw after logging
        }

        int gamesPlayed = participations.size();

        int gamesWon = calculateGamesWon(participations);

        BigDecimal totalProfit = calculateTotalProfit(username);

        BigDecimal averageProfit = gamesPlayed > 0
                ? totalProfit.divide(BigDecimal.valueOf(gamesPlayed * 10000), RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        double winRate = gamesPlayed > 0 ? (double) gamesWon / gamesPlayed * 100 : 0;

        return new PlayerPerformanceDto(username, gamesPlayed, gamesWon, winRate, totalProfit, averageProfit);
    }

    private BigDecimal calculateTotalProfit(String username) {
        log.info("Calculating total profit for username: {}", username);

        List<GamePortfolio> portfolios;
        try {
            portfolios = gamePortfolioRepository.findByUserUsername(username);
        } catch (IncorrectResultSizeDataAccessException e) {
            throw e;
        }

        BigDecimal totalProfit = portfolios.stream()
                .map(this::calculateProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalProfit;
    }

    private int calculateGamesWon(List<GameParticipation> participations) {

        int gamesWon = (int) participations.stream()
                .filter(participation -> {
                    boolean isCompleted = participation.getGame().getStatus() == GameStatus.COMPLETED;
                    log.debug("Game status for participation {}: {}", participation.getId(), isCompleted ? "COMPLETED" : "NOT COMPLETED");
                    return isCompleted;
                })
                .filter(this::didPlayerWin)
                .count();

        log.info("Total games won: {}", gamesWon);
        return gamesWon;
    }

    private boolean didPlayerWin(GameParticipation participation) {

        List<GamePortfolio> portfolios = gamePortfolioRepository.findAllByUserAndGame(participation.getUser(), participation.getGame());

        if (portfolios.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("Multiple portfolios found", 1);
        }

        GamePortfolio portfolio = portfolios.stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Portfolio not found"));

        BigDecimal profit = calculateProfit(portfolio);


        return profit.compareTo(BigDecimal.ZERO) > 0;
    }





    private int calculateRank(Long gameId, GamePortfolio userPortfolio) {
        List<GamePortfolio> portfolios = gamePortfolioRepository.findByGameId(gameId);

        List<GamePortfolio> rankedPortfolios = portfolios.stream()
                .sorted(Comparator.comparing(this::calculateProfit).reversed())
                .collect(Collectors.toList());

        return rankedPortfolios.indexOf(userPortfolio) + 1;
    }

    @Scheduled(fixedRate = 6000)
    public void endScheduledGames() {
        logger.info("Scheduled task is running...");
        List<Game> gamesToEnd = gameRepository.findByStatusAndEndTimestampBefore(GameStatus.ACTIVE, LocalDateTime.now());
        gamesToEnd.forEach(game -> {
            try {
                endGame(game.getId());
            } catch (Exception e) {
                logger.error("Error ending game with ID {}: {}", game.getId(), e.getMessage());
            }
        });
    }
    @Transactional
    public void endGame(Long gameId) {

        logger.info("Attempting to end the game with ID: {}", gameId);

        // Fetch the game entity
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> {
                    logger.error("No game found with ID: {}", gameId);
                    return new GameNotFoundException("No game found with ID: " + gameId);
                });

        // Check if the game is already completed
        if (game.getStatus() == GameStatus.COMPLETED) {
            logger.warn("Game with ID: {} is already completed.", gameId);
            throw new IllegalStateException("Game is already completed.");
        }

        // Update game status to COMPLETED
        game.setStatus(GameStatus.COMPLETED);
        gameRepository.save(game);
        logger.info("Game with ID: {} has been marked as completed.", gameId);

        // Retrieve portfolios of all participants
        List<GamePortfolio> portfolios = gamePortfolioRepository.findByGame(game);
        logger.info("Retrieved portfolios for {} participants in game ID: {}", portfolios.size(), gameId);
        portfolios.forEach(portfolio -> Hibernate.initialize(portfolio.getGameHoldings()));

        // Calculate profits for each portfolio and sort by profits
        List<LeaderboardEntryDto> leaderboard = portfolios.stream()
                .map(portfolio -> {
                    BigDecimal profit = calculateProfits(portfolio);
                    logger.debug("User: {} - Calculated Profit: {}", portfolio.getUser().getUsername(), profit);
                    return new LeaderboardEntryDto(portfolio.getUser().getUsername(), profit);
                })
                .sorted(Comparator.comparing(LeaderboardEntryDto::getProfit).reversed())
                .collect(Collectors.toList());

        logger.info("Leaderboard has been calculated and sorted.");

        // Distribute rewards based on leaderboard ranking
        logger.info("Distributing rewards based on leaderboard ranking.");
        distributeRewards(leaderboard, portfolios);
        logger.info("Rewards have been distributed.");

        // Log the leaderboard for archival or further processing
        logger.info("Final Leaderboard: ");
        leaderboard.forEach(entry -> logger.info("User: {} - Profit: {}", entry.getUsername(), entry.getProfit()));
    }

    private BigDecimal calculateProfits(GamePortfolio portfolio) {
        BigDecimal initialCash = BigDecimal.valueOf(10000); // Example initial cash
        BigDecimal currentCash = BigDecimal.valueOf(portfolio.getCash());

        // Calculate the total value of holdings based on the last recorded price of each asset in the game
        BigDecimal holdingsValue = portfolio.getGameHoldings().stream()
                .map(holding -> {
                    MarketData lastMarketData = marketDataRepository.findTopByAssetAndGameOrderByTimestampDesc(
                            holding.getAsset(), portfolio.getGame()
                    );
                    BigDecimal lastPrice = BigDecimal.valueOf(lastMarketData.getClose());
                    return lastPrice.multiply(BigDecimal.valueOf(holding.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return currentCash.add(holdingsValue).subtract(initialCash);
    }


    private void distributeRewards(List<LeaderboardEntryDto> leaderboard, List<GamePortfolio> portfolios) {
        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardEntryDto entry = leaderboard.get(i);
            String username = entry.getUsername();

            // Retrieve the User entity
            GamePortfolio gamePortfolio = portfolios.stream()
                    .filter(p -> p.getUser().getUsername().equals(username))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("GamePortfolio not found for user: " + username));
            User user = gamePortfolio.getUser();

            // Retrieve the User's Portfolio
            Portfolio userPortfolio = user.getPortfolio();
            if (userPortfolio == null) {
                throw new EntityNotFoundException("Portfolio not found for user: " + username);
            }

            // Add rewards to the user's portfolio cash
            BigDecimal currentCash = BigDecimal.valueOf(userPortfolio.getCash());
            BigDecimal reward = BigDecimal.ZERO;

            // Reward logic based on leaderboard ranking
            if (i == 0) {
                reward = BigDecimal.valueOf(5000); // 1st place bonus
            } else if (i == 1) {
                reward = BigDecimal.valueOf(3000); // 2nd place bonus
            } else if (i == 2) {
                reward = BigDecimal.valueOf(1000); // 3rd place bonus
            }

            // Update the portfolio cash
            userPortfolio.setCash(currentCash.add(reward).doubleValue());
            logger.info("Reward of {} has been added to {}'s portfolio. New cash balance: {}", reward, username, userPortfolio.getCash());

            // Save the updated portfolio
            portfolioRepository.save(userPortfolio);
        }


    }


    public boolean updateGameParticipationTimestamp(Long gameParticipationId, LocalDateTime lastPauseTimestamp) {
        // Fetch the participation record from the database
        var participation = gameParticipationRepository.findById(gameParticipationId);

        if (participation.isPresent()) {
            // Update the timestamp and save
            participation.get().setLastPauseTimestamp(lastPauseTimestamp);
            gameParticipationRepository.save(participation.get());
            return true;
        }

        return false;
    }
    public List<NewsArticleResponseDto> getNewsByGameId(Long gameId) {
        List<NewsArticle> newsArticles = newsArticleRepository.findByGameId(gameId);
        return newsArticles.stream()
                .map(news -> NewsArticleResponseDto.builder()
                        .id(news.getId())
                        .category(news.getCategory())
                        .headline(news.getHeadline())
                        .source(news.getSource())
                        .related(news.getRelated())
                        .url(news.getUrl())
                        .image(news.getImage())
                        .publishedDate(news.getPublishedDate())
                        .build())
                .collect(Collectors.toList());
    }

}
