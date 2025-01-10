package tn.zeros.marketmaster.service;

import io.netty.handler.timeout.ReadTimeoutException;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import java.io.IOException;

import java.util.concurrent.TimeUnit;
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

        log.info("Calculating option premium for request: {}", requestBody);

        return webClient.post()
                .uri("/api/assets/calculate_option_premium")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof ReadTimeoutException
                                || throwable instanceof IOException)
                        .doBeforeRetry(retrySignal ->
                                log.warn("Retrying request after error, attempt {}",
                                        retrySignal.totalRetries() + 1)))
                .timeout(Duration.ofSeconds(15))
                .map(responseMap -> {
                    log.info("Received response: {}", responseMap);
                    if (!responseMap.containsKey("premium")) {
                        throw new IllegalStateException("Response missing premium");
                    }

                    Object premiumValue = responseMap.get("premium");
                    if (premiumValue == null) {
                        throw new IllegalStateException("Premium value is null");
                    }

                    double premium;
                    if (premiumValue instanceof Number) {
                        premium = ((Number) premiumValue).doubleValue();
                    } else if (premiumValue instanceof String) {
                        premium = Double.parseDouble((String) premiumValue);
                    } else {
                        throw new IllegalStateException("Invalid premium value type: " +
                                premiumValue.getClass().getName());
                    }

                    if (premium <= 0) {
                        throw new IllegalStateException("Invalid premium value: " + premium);
                    }

                    return premium;
                })
                .doOnError(error -> log.error("Error calculating premium: {}",
                        error.getMessage()))
                .onErrorMap(ReadTimeoutException.class, ex ->
                        new FlaskServiceRegistrationException(
                                "Flask service timeout after retries: " + ex.getMessage()))
                .onErrorMap(Exception.class, ex ->
                        new FlaskServiceRegistrationException(
                                "Flask service error: " + ex.getMessage()))
                .block(Duration.ofSeconds(20));  // Overall timeout including retries
    }
    public TransactionDTO applyOption(Option option ,String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));


        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + username);
        }
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setPrice(option.getStrikePrice()*100);
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
