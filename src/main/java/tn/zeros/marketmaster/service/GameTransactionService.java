package tn.zeros.marketmaster.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.GameTransactionDto;
import tn.zeros.marketmaster.entity.*;
import tn.zeros.marketmaster.entity.enums.TransactionType;
import tn.zeros.marketmaster.exception.GameNotFoundException;
import tn.zeros.marketmaster.repository.*;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameTransactionService {
    private final GameRepository gameRepository;
    private final AssetRepository assetRepository;
    private final GamePortfolioRepository gamePortfolioRepository;
    private final MarketDataRepository marketDataRepository;
    private final GameTransactionRepository gameTransactionRepository;
    private final UserRepository userRepository;



    @Transactional
    public void processTransaction(GameTransactionDto request) {
        // Retrieve the game
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + request.getGameId()));

        // Retrieve the user by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + request.getUsername()));

        // Retrieve the asset
        Asset asset = assetRepository.findBySymbol(request.getSymbol());


        // Retrieve the portfolio for the game and user
        GamePortfolio portfolio = gamePortfolioRepository.findByGameAndUser(game, user)
                .orElseThrow(() -> new UsernameNotFoundException("Portfolio not found for game and user"));

        // Get the latest market data before the simulationTimestamp
        MarketData marketData = marketDataRepository.findTopByAssetAndGameAndTimestampBeforeOrderByTimestampDesc(
                        asset, game, request.getSimulationTimestamp())
                .orElseThrow(() -> new GameNotFoundException("No market data found for asset before timestamp"));

        // Determine the price and total cost of the transaction
        double price = marketData.getClose();
        double totalCost = price * request.getQuantity();

        if (request.getType() == TransactionType.BUY) {
            processBuyTransaction(portfolio, asset, request.getQuantity(), price, totalCost);
        } else if (request.getType() == TransactionType.SELL) {
            processSellTransaction(portfolio, asset, request.getQuantity(), price, totalCost);
        } else {
            throw new IllegalArgumentException("Invalid transaction type");
        }

        // Save the transaction
        GameTransaction transaction = new GameTransaction();
        transaction.setGamePortfolio(portfolio);
        transaction.setAsset(asset);
        transaction.setType(request.getType());
        transaction.setQuantity(request.getQuantity());
        transaction.setPrice(price);
        transaction.setSimulationTimestamp(request.getSimulationTimestamp());

        gameTransactionRepository.save(transaction);
    }

    private void processBuyTransaction(GamePortfolio portfolio, Asset asset, int quantity, double price, double totalCost) {
        // Check if portfolio has enough cash
        if (portfolio.getCash() < totalCost) {
            throw new IllegalStateException("Not enough cash in portfolio to complete the transaction");
        }

        // Deduct cash from portfolio
        portfolio.setCash(portfolio.getCash() - totalCost);

        // Update holdings
        Optional<GameHolding> existingHolding = portfolio.getGameHoldings().stream()
                .filter(h -> h.getAsset().equals(asset))
                .findFirst();

        if (existingHolding.isPresent()) {
            GameHolding holding = existingHolding.get();
            BigDecimal newQuantity = BigDecimal.valueOf(holding.getQuantity() + quantity);
            BigDecimal newCostBasis = BigDecimal.valueOf((holding.getAverageCostBasis().doubleValue() * holding.getQuantity() + totalCost) / newQuantity.doubleValue());

            holding.setQuantity(newQuantity.intValue());
            holding.setAverageCostBasis(newCostBasis);
        } else {
            GameHolding newHolding = new GameHolding();
            newHolding.setGamePortfolio(portfolio);
            newHolding.setAsset(asset);
            newHolding.setQuantity(quantity);
            newHolding.setAverageCostBasis(BigDecimal.valueOf(price));
            portfolio.getGameHoldings().add(newHolding);
        }

        gamePortfolioRepository.save(portfolio);
    }

    private void processSellTransaction(GamePortfolio portfolio, Asset asset, int quantity, double price, double totalRevenue) {
        // Find existing holding
        GameHolding holding = portfolio.getGameHoldings().stream()
                .filter(h -> h.getAsset().equals(asset))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No holdings found for asset in portfolio"));

        if (holding.getQuantity() < quantity) {
            throw new IllegalStateException("Not enough holdings to sell");
        }

        // Update holding quantity
        holding.setQuantity(holding.getQuantity() - quantity);

        // Add revenue to portfolio cash
        portfolio.setCash(portfolio.getCash() + totalRevenue);

        // Remove holding if quantity is zero
        if (holding.getQuantity() == 0) {
            portfolio.getGameHoldings().remove(holding);
        }

        gamePortfolioRepository.save(portfolio);
    }
}
