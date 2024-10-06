package tn.zeros.marketmaster.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.ServiceInterface.IPortfolioService;
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
public class PortfolioService implements IPortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;

@Override //Calculate Gain For User
    public Double calculatePortfolioGainForUser(Long userId) {
    // Fetch the portfolio by user ID
    Optional<Portfolio> portfolioOptional = portfolioRepository.findByUserId(userId);

    if (portfolioOptional.isPresent()) {
        Portfolio portfolio = portfolioOptional.get();
        List<Holding> holdings = portfolio.getHoldings();
        Double s = 0D;
        for (Holding h : holdings) {
            s += (h.getCurrentValue() - h.getPurchasePrice()) * h.getQuantity();
        }
        return s;
    } else {
        throw new RuntimeException("Portfolio not found for user ID: " + userId);
    }

}
    @Override //Calculate Holding For User
    public Double calculatePortfolioHolding(Long userId) {

        Optional<Portfolio> portfolioOptional = portfolioRepository.findByUserId(userId);

        if (portfolioOptional.isPresent()) {
            Portfolio portfolio = portfolioOptional.get();
            List<Holding> holdings = portfolio.getHoldings();
            Double s = 0D;
            for (Holding h : holdings) {
                s += h.getPurchasePrice() * h.getQuantity();
            }
            return s;
        } else {
            throw new RuntimeException("Portfolio not found for user ID: " + userId);
        }

    }
@Override //Update Portfolio
    public void updatePortfolio(Long userId) {

        // Fetch the portfolio by the user's ID
        Optional<Portfolio> portfolioOptional = portfolioRepository.findByUserId(userId);
        if (portfolioOptional.isPresent()) {
            Portfolio portfolio = portfolioOptional.get();
            portfolio.setCurrentRank(portfolioRepository.getRankByUserId(userId));
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime yearAgo = today.minusDays(365);
            Double todayValue = portfolio.getTotalValue().get(today);
            Double yearAgoValue =portfolio.getTotalValue().get(portfolio.getCreatedAt());

            //update rank
            portfolio.setCurrentRank(portfolioRepository.getRankByUserId(userId));


            //change Cash
            portfolio.setCash(todayValue-calculatePortfolioHolding(userId));

            //change ChangeOfToday
            portfolio.setChangeOfToday(calculatePortfolioGainForUser(userId));

            //Change  AnnualReturn
            if ( portfolio.getTotalValue().get(yearAgo) !=null){
                yearAgoValue = portfolio.getTotalValue().get(yearAgo);
            }
            if (todayValue != null && yearAgoValue != null) {
                Double total = todayValue - yearAgoValue;
                Double pourcentage = (total *100)/yearAgoValue;
                portfolio.setAnnualReturn(pourcentage);
                portfolioRepository.save(portfolio);
            } else {
                portfolio.setAnnualReturn(null);
                portfolioRepository.save(portfolio);
            }
        } else {
            // Handle case where portfolio is not found for the user ID
            throw new RuntimeException("Portfolio not found for user ID: " + userId);
        }
    }
@Override //Add New Portfolio For User
    public void newPortfolio(Long userId){
    User U= userRepository.getById(userId);
    Portfolio p =new Portfolio();
    p.setTotalValue(new HashMap<>()); // Initialize the map
    p.getTotalValue().put(LocalDateTime.now(), 100000D);
    p.setCash(100000D);
    p.setAnnualReturn(0.0D);
    p.setCurrentRank(portfolioRepository.getRankByUserId(userId));
    //List<Holding> H= new ArrayList<>();
    Holding h = new Holding();
    p.getHoldings().add(h);
    portfolioRepository.save(p);
    U.setPortfolio(p);
    userRepository.save(U);
}



}
