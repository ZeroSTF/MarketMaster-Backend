package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.zeros.marketmaster.entity.GamePortfolio;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class GamePortfolioDto {
    private double cash;
    private List<GameHoldingDto> holdings;
    private List<GameTransactionDto> transactions;

    public GamePortfolioDto(GamePortfolio portfolio) {
        this.cash = portfolio.getCash();
        this.holdings = portfolio.getGameHoldings().stream()
                .map(GameHoldingDto::new)
                .collect(Collectors.toList());
        this.transactions = portfolio.getGameTransactions().stream()
                .map(GameTransactionDto::new)
                .collect(Collectors.toList());
    }
}

