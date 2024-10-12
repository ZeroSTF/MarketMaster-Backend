package tn.zeros.marketmaster.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.HoldingDTO;
import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.Holding;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.AssetNotFoundException;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.AssetRepository;
import tn.zeros.marketmaster.repository.PortfolioRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class PortfolioService  {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    private final AssetService assetService;

    public double calculateGainLoss(Long userId, Duration duration) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        if (portfolio == null || portfolio.getTotalValue().isEmpty()) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minus(duration);

        Map<LocalDateTime, Double> totalValue = portfolio.getTotalValue();

        Map.Entry<LocalDateTime, Double> startEntry = totalValue.entrySet().stream()
                .filter(entry -> !entry.getKey().isAfter(startDate))
                .max(Map.Entry.comparingByKey())
                .orElse(null);

        Map.Entry<LocalDateTime, Double> endEntry = totalValue.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .orElse(null);

        if (startEntry == null || endEntry == null) {
            return 0.0;
        }

        double startValue = startEntry.getValue();
        double endValue = endEntry.getValue();

        return endValue - startValue;
    }

    public double calculatePortfolioHolding(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        if (portfolio == null || portfolio.getHoldings().isEmpty()) {
            return 0.0;
        }

        double totalHoldingValue = portfolio.getHoldings().stream()
                .mapToDouble(holding -> {
                    Asset asset = holding.getAsset();
                    int quantity = holding.getQuantity();
                    double currentPrice = assetService.getCurrentPrice(asset.getId());
                    return quantity * currentPrice;
                })
                .sum();

        totalHoldingValue += portfolio.getCash();

        return totalHoldingValue;
    }

    public PortfolioDTO updatePortfolio(PortfolioDTO portfolioDTO) {
        Portfolio portfolio = portfolioRepository.findById(portfolioDTO.getId())
                .orElseThrow(() -> new PortfolioNotFoundException("Portfolio not found"));

        portfolio.setCash(portfolioDTO.getCash());

        Set<Holding> updatedHoldings = new HashSet<>();
        for (HoldingDTO holdingDTO : portfolioDTO.getHoldings()) {
            Holding holding = portfolio.getHoldings().stream()
                    .filter(h -> h.getId().equals(holdingDTO.getId()))
                    .findFirst()
                    .orElseGet(Holding::new);

            holding.setQuantity(holdingDTO.getQuantity());
            holding.setPortfolio(portfolio);
            holding.setAsset(assetRepository.findById(holdingDTO.getAssetId())
                    .orElseThrow(() -> new AssetNotFoundException("Asset not found")));

            updatedHoldings.add(holding);
        }
        portfolio.setHoldings(updatedHoldings);

        // Update total value
        portfolio.setTotalValue(portfolioDTO.getTotalValue());

        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return PortfolioDTO.fromEntity(updatedPortfolio);
    }

    public PortfolioDTO newPortfolio(PortfolioDTO portfolioDTO) {
        User user = userRepository.findById(portfolioDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);
        portfolio.setCash(portfolioDTO.getCash());

        Set<Holding> holdings = new HashSet<>();
        for (HoldingDTO holdingDTO : portfolioDTO.getHoldings()) {
            Holding holding = new Holding();
            holding.setQuantity(holdingDTO.getQuantity());
            holding.setPortfolio(portfolio);
            holding.setAsset(assetRepository.findById(holdingDTO.getAssetId())
                    .orElseThrow(() -> new AssetNotFoundException("Asset not found")));
            holdings.add(holding);
        }
        portfolio.setHoldings(holdings);

        portfolio.setTotalValue(portfolioDTO.getTotalValue());

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return PortfolioDTO.fromEntity(savedPortfolio);
    }


}
