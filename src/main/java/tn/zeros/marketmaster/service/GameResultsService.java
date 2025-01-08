package tn.zeros.marketmaster.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import tn.zeros.marketmaster.dto.GameResultsDto;
import tn.zeros.marketmaster.dto.GameHoldingDto;
import tn.zeros.marketmaster.entity.*;
import tn.zeros.marketmaster.exception.GameNotFoundException;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameResultsService {

    private final GamePortfolioRepository gamePortfolioRepository;
    private final MarketDataRepository marketDataRepository;
    private final GameParticipationRepository gameParticipationRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public GameResultsService(
            GamePortfolioRepository gamePortfolioRepository,
            MarketDataRepository marketDataRepository,
            GameParticipationRepository gameParticipationRepository,
            UserRepository userRepository,
            GameRepository gameRepository) {
        this.gamePortfolioRepository = gamePortfolioRepository;
        this.marketDataRepository = marketDataRepository;
        this.gameParticipationRepository = gameParticipationRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public GameResultsDto getGameResults(Long gameId, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("user not found "));
        Game game = gameRepository.findById(gameId).orElseThrow(()->new GameNotFoundException("game not found "));
        GamePortfolio portfolio = gamePortfolioRepository.findByUserAndGame(user, game).orElseThrow(()->new PortfolioNotFoundException("portfolio not found"));


        BigDecimal initialCash = BigDecimal.valueOf(10000);
        BigDecimal finalCash = BigDecimal.valueOf(portfolio.getCash());
        BigDecimal totalHoldingsValue = calculateTotalHoldingsValue(portfolio);
        BigDecimal totalValue = finalCash.add(totalHoldingsValue);

        // Calculate profit percentage
        BigDecimal profit = totalValue.subtract(initialCash);
        BigDecimal profitPercentage = profit.divide(initialCash, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // Prepare holdings DTOs
        List<GameHoldingDto> holdings = portfolio.getGameHoldings().stream()
                .map(GameHoldingDto::new) // Use the existing GameHoldingDto constructor
                .collect(Collectors.toList());

        // Build the results DTO
        GameResultsDto results = new GameResultsDto();
        results.setFinalCash(finalCash);
        results.setHoldings(holdings);
        results.setTotalHoldingsValue(totalHoldingsValue);
        results.setProfitPercentage(profitPercentage);

        return results;
    }

    private BigDecimal calculateTotalHoldingsValue(GamePortfolio portfolio) {
        return portfolio.getGameHoldings().stream()
                .map(holding -> calculateHoldingFinalValue(holding, portfolio.getGame()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateHoldingFinalValue(GameHolding holding, Game game) {
        Asset asset = holding.getAsset();
        GameParticipation participation = gameParticipationRepository.findByGameAndUser(game, holding.getGamePortfolio().getUser())
                .orElseThrow(() -> new EntityNotFoundException("Participation not found"));

        MarketData latestMarketData = marketDataRepository.findTopByAssetAndGameAndTimestampBeforeOrderByTimestampDesc(
                asset, game, participation.getLastPauseTimestamp()
        ).orElseThrow(() -> new EntityNotFoundException("No market data found"));

        BigDecimal latestPrice = BigDecimal.valueOf(latestMarketData.getClose());
        return latestPrice.multiply(BigDecimal.valueOf(holding.getQuantity()));
    }
}