package tn.zeros.marketmaster.service;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.TransactionDTO;
import tn.zeros.marketmaster.entity.*;
import tn.zeros.marketmaster.exception.*;
import tn.zeros.marketmaster.repository.*;
import java.math.BigDecimal;
import java.util.*;
@Slf4j
@Service
@AllArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private  final HoldingRepository holdingRepository;
    private final AssetRepository assetRepository;
    private final AssetService assetService;
    private final HoldingService holdingService;

    public List<TransactionDTO> getStatBySymbol(String username, String symbol){
        User user = userRepository.findByUsername(username)
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
    public TransactionDTO addTransaction(String username, TransactionDTO transactionDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + username);
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
        double totalCost = transactionDTO.getQuantity() * transactionDTO.getPrice();
        if (portfolio.getCash() < totalCost) {
            throw new InsufficientFundsException("Not enough cash for this transaction");
        }

        portfolio.setCash(portfolio.getCash() - totalCost);
        Asset asset = assetRepository.findBySymbol(transactionDTO.getSymbol());
        Holding holding = findOrCreateHolding(portfolio, asset);
        Transaction transaction = transactionDTO.toEntity();
        transaction.setPrice(transactionDTO.getPrice());
        transaction.setPortfolio(portfolio);
        transaction.setAsset(asset);
        portfolio.getTransactions().add(transaction);
        holdingService.updateAverageCostBasis(holding.getId(),BigDecimal.valueOf(transaction.getPrice()),transactionDTO.getQuantity());
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
        double totalProceeds = transactionDTO.getQuantity() * transactionDTO.getPrice();
        portfolio.setCash(portfolio.getCash() + totalProceeds);

        Transaction transaction = transactionDTO.toEntity();
        transaction.setPrice(transactionDTO.getPrice());
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
                    newHolding.setAverageCostBasis(BigDecimal.ZERO);
                    holdingRepository.save(newHolding);
                    portfolio.getHoldings().add(newHolding);
                    return newHolding;
                });
    }

    private boolean isHoldingAttributesEmpty(Holding holding) {
        return (holding.getQuantity() == null)  &&
                (holding.getAverageCostBasis() == null );
    }

    public TransactionDTO findMaxQuantity(String symbol,String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found for username: " + username));
        Portfolio portfolio = user.getPortfolio();
        TransactionDTO transactionDTO=new TransactionDTO();
        Holding holding=new Holding();
        Set<Holding> holdings = portfolio.getHoldings();
        for(Holding holding1 : holdings) {
            if(holding1.getAsset().equals(assetRepository.findBySymbol(symbol))) {
               holding=holding1;
            }
        }
        if (isHoldingAttributesEmpty(holding)) {
            transactionDTO.setPrice(portfolio.getCash());
        }else{
        transactionDTO.setSymbol(symbol);
        transactionDTO.setQuantity(holding.getQuantity());
        transactionDTO.setPrice(portfolio.getCash());}
        return transactionDTO;
    }
}
