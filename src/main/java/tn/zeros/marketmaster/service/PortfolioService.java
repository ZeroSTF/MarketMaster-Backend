package tn.zeros.marketmaster.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.entity.Holding;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.repository.HoldingRepository;
import tn.zeros.marketmaster.repository.PortfolioRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PortfolioService  {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;

 //Calculate Gain For User
    public double calculatePortfolioGainForUser(Long userId) {
    // Fetch the portfolio by user ID
    Optional<Portfolio> portfolioOptional = portfolioRepository.findByUserId(userId);

    if (portfolioOptional.isPresent()) {
        Portfolio portfolio = portfolioOptional.get();
        List<Holding> holdings = portfolio.getHoldings();
        double s = 0D;
        for (Holding h : holdings) {
            s += (h.getCurrentValue() - h.getPurchasePrice()) * h.getQuantity();
        }
        return s;
    } else {
        throw new RuntimeException("Portfolio not found for user ID: " + userId);
    }

}
   //Calculate Holding For User
    public double calculatePortfolioHolding(Long userId) {

        Optional<Portfolio> portfolioOptional = portfolioRepository.findByUserId(userId);

        if (portfolioOptional.isPresent()) {
            Portfolio portfolio = portfolioOptional.get();
            List<Holding> holdings = portfolio.getHoldings();
            double s = 0D;
            for (Holding h : holdings) {
                s += h.getPurchasePrice() * h.getQuantity();
            }
            return s;
        } else {
            throw new RuntimeException("Portfolio not found for user ID: " + userId);
        }

    }
 //Update Portfolio
    public PortfolioDTO updatePortfolio(Long userId) {

        // Fetch the portfolio by the user's ID
    Optional<Portfolio> portfolioOptional = portfolioRepository.findByUserId(userId);
    if (portfolioOptional.isPresent()) {
        Portfolio portfolio = portfolioOptional.get();

        // Set the current rank
        portfolio.setCurrentRank(portfolioRepository.getRankByUserId(userId));

        // Define the dates: today and a year ago
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime yearAgo = today.minusDays(365);

        // Fetch today's value from the totalValue map
        Double todayValue = portfolio.getTotalValue().get(today);

        // If no value exists for 'today', set it to 0
        if (todayValue == null) {
            todayValue = 0D;
        }

        // Fetch the value from a year ago, default to the portfolio creation date if necessary
        Double yearAgoValue = portfolio.getTotalValue().get(yearAgo);
        if (yearAgoValue == null) {
            yearAgoValue = portfolio.getTotalValue().get(portfolio.getCreatedAt());
        }

        // Update rank again
        portfolio.setCurrentRank(portfolioRepository.getRankByUserId(userId));

        // Update cash by subtracting the calculated holding value from today's total value
        portfolio.setCash(todayValue - calculatePortfolioHolding(userId));

        // Update the change of today using the calculated gain
        portfolio.setChangeOfToday(calculatePortfolioGainForUser(userId));

        // Calculate the annual return if both values are available
        if (todayValue != null && yearAgoValue != null && yearAgoValue != 0) {
            double total = todayValue - yearAgoValue;
            double percentage = (total * 100) / yearAgoValue;
            // Ensure percentage is non-negative
            portfolio.setAnnualReturn(Math.max(percentage, 0));
        } else {
            portfolio.setAnnualReturn(0D);
        }

        // Save the portfolio updates
        Portfolio updatedPortfolio = portfolioRepository.save(portfolio);
        return PortfolioDTO.fromEntity(updatedPortfolio);

    } else {
        // Handle case where portfolio is not found for the user ID
        throw new RuntimeException("Portfolio not found for user ID: " + userId);
    }
    }
//Add New Portfolio For User
    public PortfolioDTO newPortfolio(Long userId){
    User user= userRepository.findById(userId).orElseThrow(() ->new UsernameNotFoundException("No user found"));
        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(new HashMap<>());
        portfolio.getTotalValue().put(LocalDateTime.now(), 100000D);
        portfolio.setCash(100000D);
        portfolio.setAnnualReturn(0.0D);
        portfolio.setCurrentRank(portfolioRepository.getRankByUserId(userId));
        portfolio.setHoldings(new ArrayList<>());
        portfolio.setUser(user);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);
        return PortfolioDTO.fromEntity(savedPortfolio);
}



}
