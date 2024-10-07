package tn.zeros.marketmaster.ServiceInterface;

import tn.zeros.marketmaster.entity.Portfolio;

public interface IPortfolioService {


    //Calculate Gain For User
    double calculatePortfolioHolding(Long userId);

    void updatePortfolio(Long userId);

    void newPortfolio(Long userId);

    double calculatePortfolioGainForUser(Long userId);
}
