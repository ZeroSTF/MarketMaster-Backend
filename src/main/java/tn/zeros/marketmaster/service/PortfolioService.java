package tn.zeros.marketmaster.service;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.OverviewDTO;
import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.entity.*;
import tn.zeros.marketmaster.entity.enums.TransactionType;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.HoldingRepository;
import tn.zeros.marketmaster.repository.PortfolioRepository;
import tn.zeros.marketmaster.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AssetService assetService;
    private final HoldingRepository holdingRepository;

    private static final String USER_NOT_FOUND_MSG = "User not found";

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

        overview.setAnnualReturn(calculateReturn(username, 1));
        overview.setCash(portfolio.getCash());

        double totalValueToday = portfolio.getTotalValue().get(getStartOfDay());
        double totalValueYesterday = portfolio.getTotalValue().get(getStartOfYesterday());

        overview.setTotalValue(totalValueToday);
        overview.setCashPercentage((portfolio.getCash() - calculateCashYesterday(username)) * 100 / calculateCashYesterday(username));
        overview.setTotalValuePercentage((totalValueToday - totalValueYesterday) * 100 / totalValueToday);
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

}

