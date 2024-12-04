package tn.zeros.marketmaster.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.entity.Option;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.FlaskServiceRegistrationException;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.OptionRepository;
import tn.zeros.marketmaster.repository.PortfolioRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class OptionService {
    private final OptionRepository optionRepository;
    private final PortfolioService portfolioService;
    private final  UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final WebClient webClient;

    public Option buyOption(String username, Option option) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));


        Portfolio portfolio = user.getPortfolio();
        if (portfolio == null) {
            throw new PortfolioNotFoundException("Portfolio not found for user: " + username);
        }


        if (portfolio.getCash() < option.getPremium()) {
            throw new IllegalArgumentException("Not enough cash in the portfolio to buy the option.");
        }
        Double prime= this.getOptionPrime(option);
        option.setPremium(prime);
        portfolio.setCash(portfolio.getCash() - option.getPremium());


        Option savedOption = optionRepository.save(option);


        portfolio.getOptions().add(savedOption);


        portfolioRepository.save(portfolio);


        return savedOption;
    }
    public Double getOptionPrime(Option option) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/assets/premiums")
                        .queryParam("symbol", option.getSymbol())
                        .queryParam("strike_price", option.getStrikePrice())
                        .queryParam("option_type", option.getType())
                        .queryParam("expiration_date", option.getDateEcheance())
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(String.class) // Assumes error body is returned as a String
                                .flatMap(errorResponse -> {
                                    log.error("Error response from Flask: {}", errorResponse);
                                    return Mono.error(new FlaskServiceRegistrationException("Flask service registration failed: " + errorResponse));
                                })
                )
                .bodyToMono(Double.class)
                .doOnSubscribe(subscription ->
                        log.info("Sending request to Flask API for symbol: {}", option.getSymbol()))
                .doOnSuccess(result ->
                        log.info("Successfully received option premiums for symbol: {}", option.getSymbol()))
                .doOnError(error ->
                        log.error("Error calling Flask API: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Returning default value due to error for symbol: {}", option.getSymbol());
                    return Mono.just(0.0); // Default value in case of error
                })
                .block(); // This makes the call synchronous and returns the value directly
    }
}
