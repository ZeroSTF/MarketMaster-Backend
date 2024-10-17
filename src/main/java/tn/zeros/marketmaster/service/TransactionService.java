package tn.zeros.marketmaster.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.HoldingDTO;
import tn.zeros.marketmaster.dto.TransactionDTO;
import tn.zeros.marketmaster.entity.*;
import tn.zeros.marketmaster.entity.enums.TransactionType;
import tn.zeros.marketmaster.exception.*;
import tn.zeros.marketmaster.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private  final HoldingRepository holdingRepository;
    private final AssetRepository assetRepository;
    private final AssetService assetService;

    public List<TransactionDTO> GetStatBySymbol(Long userId, String symbol){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Portfolio portfolio = user.getPortfolio();
        Set<Transaction> transactions= portfolio.getTransactions();
        List<TransactionDTO> transactionDTOS=new ArrayList<>();
        for (Transaction T:transactions) {
            if (T.getAsset().getSymbol().trim().equalsIgnoreCase(symbol.trim())){
                TransactionDTO transactionDTO= TransactionDTO.fromEntity(T);
                transactionDTOS.add(transactionDTO);
            }
        }
        return transactionDTOS;
    }

   @Transactional
    public TransactionDTO ajoutUneTransaction(Long userId, TransactionDTO transactionDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + userId);
        }

        if (transactionDTO.getType() == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        switch (transactionDTO.getType()) {
            case BUY:
                processBuyTransaction(portfolio, transactionDTO);
                break;
            case SELL:
                processSellTransaction(portfolio, transactionDTO);
                break;
            default:
                throw new TransactionIncorrectException("Invalid transaction type");
        }

        portfolioRepository.save(portfolio);
        return transactionDTO;
    }

    private void processBuyTransaction(Portfolio portfolio, TransactionDTO transactionDTO) {
        Asset asset1 = assetRepository.findBySymbol(transactionDTO.getSymbol());
        double totalCost = transactionDTO.getQuantity() * assetService.getCurrentPrice(asset1.getId());
        if (portfolio.getCash() < totalCost) {
            throw new InsufficientFundsException("Not enough cash for this transaction");
        }

        portfolio.setCash(portfolio.getCash() - totalCost);
        Asset asset = assetRepository.findBySymbol(transactionDTO.getSymbol());
        Holding holding = findOrCreateHolding(portfolio, asset);
        holding.setQuantity(holding.getQuantity() + transactionDTO.getQuantity());

        Transaction transaction = transactionDTO.toEntity();
        transaction.setPrice(assetService.getCurrentPrice(asset1.getId()));
        transaction.setPortfolio(portfolio);
        transaction.setAsset(asset);
        portfolio.getTransactions().add(transaction);
    }

    private void processSellTransaction(Portfolio portfolio, TransactionDTO transactionDTO) {
        Asset asset = assetRepository.findBySymbol(transactionDTO.getSymbol());
        Holding holding = portfolio.getHoldings().stream()
                .filter(h -> h.getAsset().equals(asset))
                .findFirst()
                .orElseThrow(() -> new InsufficientHoldingsException("No holdings found for this asset"));

        if (holding.getQuantity() < transactionDTO.getQuantity()) {
            throw new InsufficientHoldingsException("Not enough holdings for this transaction");
        }

        holding.setQuantity(holding.getQuantity() - transactionDTO.getQuantity());
        if (holding.getQuantity() == 0) {
            portfolio.getHoldings().remove(holding);
        }
        holdingRepository.save(holding); 
        Asset asset2 = assetRepository.findBySymbol(transactionDTO.getSymbol());
        double totalProceeds = transactionDTO.getQuantity() * assetService.getCurrentPrice(asset2.getId());
        portfolio.setCash(portfolio.getCash() + totalProceeds);

        Transaction transaction = transactionDTO.toEntity();
        transaction.setPrice(assetService.getCurrentPrice(asset2.getId()));
        transaction.setPortfolio(portfolio);
        transaction.setAsset(asset2);
        portfolio.getTransactions().add(transaction);

    }

    private Holding findOrCreateHolding(Portfolio portfolio, Asset asset) {
        return portfolio.getHoldings().stream()
                .filter(h -> h.getAsset().equals(asset))
                .findFirst()
                .orElseGet(() -> {
                    Holding newHolding = new Holding();
                    newHolding.setAsset(asset);
                    newHolding.setQuantity(0);
                    newHolding.setPortfolio(portfolio);
                    portfolio.getHoldings().add(newHolding);
                    return newHolding;
                });
    }
}
