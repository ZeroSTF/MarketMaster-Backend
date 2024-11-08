package tn.zeros.marketmaster.service;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.OverviewDTO;
import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.dto.TransactionDTO;
import tn.zeros.marketmaster.dto.WatchListDTO;
import tn.zeros.marketmaster.entity.*;
import tn.zeros.marketmaster.entity.enums.TransactionType;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.math.RoundingMode;
@Service
@AllArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AssetService assetService;
    private final HoldingRepository holdingRepository;
    private final AssetRepository  assetRepository;
    private final UserWatchlistRepository userWatchlistRepository;

    private static final String USER_NOT_FOUND_MSG = "User not found";

    private double roundToTwoDecimalPlaces(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP) // Rounding mode can be adjusted as needed
                .doubleValue();
    }
    private LocalDateTime getStartOfDay() {
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private LocalDateTime getStartOfYesterday() {
        return LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    public BigDecimal getDailyChange(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));
        Portfolio portfolio = user.getPortfolio();
        Set<Holding> holdings = holdingRepository.findAllByPortfolio(portfolio);

        BigDecimal dailyChange = holdings.stream()
                .map(holding -> holding.getAverageCostBasis().multiply(BigDecimal.valueOf(holding.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double priceNow = calculatePortfolioHolding(user.getUsername());
        return dailyChange.subtract(BigDecimal.valueOf(priceNow));
    }

    public double calculateReturn(String username, int year) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));
        Portfolio portfolio = user.getPortfolio();

        if (portfolio == null || portfolio.getTotalValue().isEmpty()) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.withHour(0).minusYears(year);
        Map<LocalDateTime, Double> totalValue = portfolio.getTotalValue();

        Map.Entry<LocalDateTime, Double> startEntry = totalValue.entrySet().stream()
                .filter(entry -> !entry.getKey().isAfter(startDate))
                .min(Map.Entry.comparingByKey())
                .orElse(null);

        Map.Entry<LocalDateTime, Double> endEntry = totalValue.entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .orElse(null);

        if (startEntry == null || endEntry == null) {
            return 0.0;
        }

        return endEntry.getValue() - startEntry.getValue();
    }

    public double calculateCashYesterday(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));
        Portfolio portfolio = user.getPortfolio();
        Set<Transaction> transactions = portfolio.getTransactions();

        double totalHolding = transactions.stream()
                .filter(t -> t.getTimestamp().isBefore(getStartOfDay()) && t.getTimestamp().isAfter(getStartOfYesterday()))
                .mapToDouble(t -> t.getPrice() * t.getQuantity())
                .sum();

        return totalHolding - portfolio.getTotalValue().get(getStartOfDay());
    }

    public OverviewDTO prepareOverview(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));

        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + username);
        }

        OverviewDTO overview = new OverviewDTO();


        overview.setAnnualReturn(roundToTwoDecimalPlaces(calculateReturn(username, 1)));
        overview.setCash(roundToTwoDecimalPlaces(portfolio.getCash()));
        overview.setHoldingNumber(holdingRepository.countByPortfolioId(portfolio.getId()));
        double totalValueToday = portfolio.getTotalValue().getOrDefault(getStartOfDay(), 0.0);
        double totalValueYesterday = portfolio.getTotalValue().getOrDefault(getStartOfYesterday(), 0.0);
        overview.setTotalValue(roundToTwoDecimalPlaces(totalValueToday - totalValueYesterday));
        double cashYesterday = calculateCashYesterday(username);
        overview.setCashPercentage(roundToTwoDecimalPlaces((portfolio.getCash() - cashYesterday) * 100 / cashYesterday));
        overview.setTotalValuePercentage(roundToTwoDecimalPlaces((totalValueToday - totalValueYesterday) * 100 / totalValueToday));

        overview.setDailyChange(getDailyChange(username));

        return overview;
    }

    public double calculatePortfolioHolding(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));
        Portfolio portfolio = user.getPortfolio();

        if (portfolio == null || portfolio.getHoldings().isEmpty()) {
            return 0.0;
        }

        return portfolio.getHoldings().stream()
                .mapToDouble(holding -> {
                    Asset asset = holding.getAsset();
                    int quantity = holding.getQuantity();
                    double currentPrice = assetService.getCurrentPrice(asset.getId());
                    return quantity * currentPrice;
                })
                .sum();
    }

    @Transactional
    public PortfolioDTO newPortfolio(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));

        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(new HashMap<>());
        portfolio.getTotalValue().put(getStartOfDay(), 100000D);
        portfolio.setCash(100000D);
        portfolio.setHoldings(new HashSet<>());
        portfolio.setUser(user);
        portfolio.setTransactions(new HashSet<>());

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return PortfolioDTO.fromEntity(savedPortfolio);
    }

    public double calculateInvestment(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));
        Portfolio portfolio = user.getPortfolio();
        Set<Transaction> transactions = portfolio.getTransactions();

        return transactions.stream()
                .filter(t -> t.getType().equals(TransactionType.BUY))
                .filter(t -> t.getTimestamp().isAfter(getStartOfDay()))
                .mapToDouble(t -> t.getPrice() * t.getQuantity())
                .sum();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateTotalValue() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            Portfolio portfolio = user.getPortfolio();
            double value = portfolio.getCash() + calculateInvestment(user.getUsername());
            portfolio.getTotalValue().put(LocalDateTime.now(), value);
        }
    }

    public List<Map<LocalDateTime, Double>> getTotalValueByPortfolioId(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));

        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + username);
        }

        List<Map<LocalDateTime, Double>> totalValueList = new ArrayList<>();
        totalValueList.add(portfolio.getTotalValue());

        return totalValueList;
    }

    public List<TransactionDTO> getAllTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found "));

        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + username);
        }
        Set<Transaction> transactions = portfolio.getTransactions();
        return transactions.stream()
                .map(TransactionDTO::fromEntity)
                .toList();
    }

    public WatchListDTO addWatchList(String username,String symbol) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));
        UserWatchlist userWatchlist=new UserWatchlist();
        userWatchlist.setUser(user);
        Asset asset= assetRepository.findBySymbol(symbol);
        userWatchlist.setAsset(asset);
        userWatchlistRepository.save(userWatchlist);
        return WatchListDTO.fromEntity(userWatchlist);
    }
    public List<WatchListDTO> getAllWatchList(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));
        List<UserWatchlist> userWatchlists =userWatchlistRepository.findByUser_Id(user.getId());
        List<WatchListDTO> watchListDTOs = new ArrayList<>();
        for (UserWatchlist userWatchlist : userWatchlists) {
            WatchListDTO watchListDTO = new WatchListDTO();
            watchListDTO.setId(userWatchlist.getId());
            watchListDTO.setUserId(userWatchlist.getUser().getId());
            watchListDTO.setAssetSymbol(userWatchlist.getAsset().getSymbol());
            watchListDTOs.add(watchListDTO);
        }
        return watchListDTOs;
    }
}

