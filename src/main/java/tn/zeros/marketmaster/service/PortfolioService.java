package tn.zeros.marketmaster.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.HoldingDTO;
import tn.zeros.marketmaster.dto.OverviewDTO;
import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.entity.*;
import tn.zeros.marketmaster.entity.enums.TransactionType;
import tn.zeros.marketmaster.exception.AssetNotFoundException;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.AssetRepository;
import tn.zeros.marketmaster.repository.HoldingRepository;
import tn.zeros.marketmaster.repository.PortfolioRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class PortfolioService  {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final AssetService assetService;
    private final HoldingRepository holdingRepository;

    public BigDecimal getDailyChange(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Portfolio portfolio = user.getPortfolio();
        Set<Holding> holdings = holdingRepository.findAllByPortfolio(portfolio);

        BigDecimal dailyChange = holdings.stream()
                .map(holding -> holding.getAverageCostBasis().multiply(BigDecimal.valueOf(holding.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double priceKnow = calculatePortfolioHolding(portfolio.getUser().getId());
        return dailyChange.subtract(BigDecimal.valueOf(priceKnow));
    }

    public double calculateReturn(Long userId, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        if (portfolio == null || portfolio.getTotalValue().isEmpty()) {
            return 0.0;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = LocalDateTime.now().withHour(0).minusYears(year);

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

        double startValue = startEntry.getValue();
        double endValue = endEntry.getValue();

        return endValue - startValue;
    }

    public double calculateCashYesterday(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Portfolio portfolio = user.getPortfolio();
        Set<Transaction> transactions = portfolio.getTransactions();
        LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        double totaleHolding = transactions.stream()
                .filter(t -> t.getTimestamp().isBefore(startOfToday) && t.getTimestamp().isAfter(startOfYesterday))
                .mapToDouble(t -> t.getPrice() * t.getQuantity())
                .sum();

        return totaleHolding-portfolio.getTotalValue().get(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0));
    }

    public OverviewDTO prepareOverview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Portfolio portfolio = user.getPortfolio();
        OverviewDTO overview = new OverviewDTO();
        overview.setAnnualReturn(calculateReturn(userId,1));
        overview.setCash(portfolio.getCash());
        overview.setTotalValue(portfolio.getTotalValue().get(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)));
        overview.setCashPercentage((portfolio.getCash()-calculateCashYesterday(userId))*100/calculateCashYesterday(userId));
        double totalValueToday = portfolio.getTotalValue().get(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0));
        double totalValueYesterday = portfolio.getTotalValue().get(LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        overview.setTotalValuePercentage((totalValueToday-totalValueYesterday)*100/totalValueToday);
        overview.setDailyChange(getDailyChange(userId));
        return overview;
    }

    public List<Map<LocalDateTime, Double>> getTotalValueByPortfolioId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Portfolio portfolio = user.getPortfolio();
        List<Map<LocalDateTime, Double>> totalValueList = new ArrayList<>();
        totalValueList.add(portfolio.getTotalValue());

        return totalValueList;

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

        //totalHoldingValue += portfolio.getCash();

        return totalHoldingValue;
    }

    /*public PortfolioDTO updatePortfolio(PortfolioDTO portfolioDTO) {
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
    }*/

    @Transactional
    public PortfolioDTO newPortfolio(Long userId) {
        User user= userRepository.findById(userId).orElseThrow(() ->new UsernameNotFoundException("No user found"));
        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(new HashMap<>());
        portfolio.getTotalValue().put(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0), 100000D);
        portfolio.setCash(100000D);
        portfolio.setHoldings(new HashSet<>());
        portfolio.setUser(user);

        portfolio.setTransactions(new HashSet<>());

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return PortfolioDTO.fromEntity(savedPortfolio);

    }

    public double calculeInvestisement(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Portfolio portfolio = user.getPortfolio();
        Set<Transaction> transactions = portfolio.getTransactions();
        double investisement = transactions.stream()
                .filter(t -> t.getType().equals(TransactionType.BUY))
                .filter(t->t.getTimestamp().isAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(1).withNano(0)))
                .mapToDouble(t -> t.getPrice() * t.getQuantity())
                .sum();


        return investisement;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateTotalValue(){
        List<User> users = userRepository.findAll();
        for (User u:users) {
            Portfolio portfolio=u.getPortfolio();
            Double value=portfolio.getCash()+calculeInvestisement(u.getId());
            portfolio.getTotalValue().put(LocalDateTime.now(),value);


        }
    }

}
