package tn.zeros.marketmaster.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.dto.TransactionDTO;
import tn.zeros.marketmaster.entity.Option;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.Transaction;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.enums.OptionStatus;
import tn.zeros.marketmaster.entity.enums.TransactionType;
import tn.zeros.marketmaster.exception.FlaskServiceRegistrationException;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.exception.TransactionIncorrectException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.OptionRepository;
import tn.zeros.marketmaster.repository.PortfolioRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tn.zeros.marketmaster.entity.enums.OptionType.CALL;

@Service
@AllArgsConstructor
@Slf4j
public class OptionService {
    private final OptionRepository optionRepository;
    private final PortfolioService portfolioService;
    private final  UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final AssetService assetService;
    private final WebClient webClient;
    private final TransactionService transactionService;

    public Option buyOption(String username, Option option) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));


        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + username);
        }



        Double prime= this.getOptionPrime(option);
        if (prime<=0){
            throw new IllegalArgumentException("Prime should be a positive value.");
        }
        double currentPrice = assetService.fetchCurrentPrice(option.getSymbol());
        if(currentPrice<=0){
            throw new IllegalArgumentException("CurrentPrice should be a positive value.");
        }
        option.setUnderlyingPrice(currentPrice);
        option.setPremium(prime);
        if (portfolio.getCash() < option.getPremium()) {
            throw new IllegalArgumentException("Not enough cash in the portfolio to buy the option.");
        }
        portfolio.setCash(portfolio.getCash() - option.getPremium());


        Option savedOption = optionRepository.save(option);


        portfolio.getOptions().add(savedOption);


        portfolioRepository.save(portfolio);


        return savedOption;
    }
    public Double getOptionPrime(Option option) {

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("symbol", option.getSymbol());
        requestBody.put("strike_price", String.valueOf(option.getStrikePrice()));
        requestBody.put("expiration_date", option.getDateEcheance().format(DateTimeFormatter.ISO_LOCAL_DATE));
        requestBody.put("option_type", option.getType().toString());

        return webClient.post()
                .uri("/api/assets/calculate_option_premium")
                .contentType(MediaType.APPLICATION_JSON) // Set content type to application/json
                .bodyValue(requestBody) // Pass the request body
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorResponse -> {
                                    log.error("Error response from Flask: {}", errorResponse);
                                    return Mono.error(new FlaskServiceRegistrationException("Flask service registration failed: " + errorResponse));
                                })
                )
                .bodyToMono(Map.class) // Deserialize the response as a Map
                .map(responseMap -> {
                    // Extract the premium value from the response map
                    if (responseMap.containsKey("premium")) {
                        Object premiumValue = responseMap.get("premium");
                        if (premiumValue instanceof Number) {
                            return ((Number) premiumValue).doubleValue();
                        } else {
                            throw new IllegalArgumentException("Invalid premium value format");
                        }
                    } else {
                        throw new IllegalArgumentException("Response missing 'premium' key");
                    }
                })
                .doOnSubscribe(subscription ->
                        log.info("Sending request to Flask API for symbol: {}", option.getSymbol()))
                .doOnSuccess(result ->
                        log.info("Successfully received option premium for symbol: {}, premium: {}", option.getSymbol(), result))
                .doOnError(error ->
                        log.error("Error calling Flask API: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Returning default value due to error for symbol: {}", option.getSymbol());
                    return Mono.just(0.0); // Return a default value in case of error
                })
                .block(); // Make the call synchronous // This makes the call synchronous and returns the value directly
    }
    public TransactionDTO applyOption(Option option ,String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));


        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + username);
        }
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setPrice(option.getStrikePrice());
        transactionDTO.setSymbol(option.getSymbol());
        transactionDTO.setTimeStamp(LocalDateTime.now());
        transactionDTO.setQuantity(100);
        switch (option.getType()) {
            case CALL :
                transactionDTO.setType(TransactionType.BUY);
                break;
            case PUT:
                transactionDTO.setType(TransactionType.SELL);
                break;
            default:
                throw new TransactionIncorrectException("Invalid transaction type");
        }
        TransactionDTO transactionDTOResponse = transactionService.addTransaction(username,transactionDTO);
        if(transactionDTOResponse!=null){
            option.setStatus(OptionStatus.USED);
            optionRepository.save(option);
            return transactionDTOResponse;
        }
        return null;


    }
    @Scheduled(cron = "0 0 0 * * *")
    public void expiration(){
        List<Option> options = optionRepository.findAll();
        for (Option o :options){
            if (o.getDateEcheance().isAfter(LocalDateTime.now()) && o.getStatus() == OptionStatus.PENDING){
                o.setStatus(OptionStatus.EXPIRED);
                optionRepository.save(o);
            }
        }
    }
}
