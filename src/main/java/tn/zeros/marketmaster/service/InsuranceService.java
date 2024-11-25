package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.FlaskServiceRegistrationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InsuranceService {
    private final WebClient webClient;
    private final UserService userService;

    public Mono<Map<String, Double>> getSuggestedPremiums(List<String> symbols) {
        String symbolsParam = String.join(",", symbols);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/assets/premiums")
                        .queryParam("symbols", symbolsParam)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorResponse -> {
                                    log.error("Error response from Flask: {}", errorResponse);
                                    return Mono.error(new FlaskServiceRegistrationException("Flask service registration failed: " + errorResponse));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Double>>() {})
                .doOnSubscribe(subscription ->
                        log.info("Sending request to Flask API for symbols: {}", symbols))
                .doOnSuccess(result ->
                        log.info("Successfully received premiums for symbols: {}", symbols))
                .doOnError(error ->
                        log.error("Error calling Flask API: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Returning empty map due to error for symbols: {}", symbols);
                    return Mono.just(new HashMap<>());
                });
    }

    public double getTotalPremiums(String username) {
        try {
            User user = userService.getUserByUseranme(username); // Assuming this returns a User object
            if (user == null) {
                log.warn("No user found with username: {}", username);
                return 0.0;
            }

            Portfolio portfolio = user.getPortfolio();
            if (portfolio == null || portfolio.getId() == null) {
                log.warn("No portfolio found for user: {}", username);
                return 0.0;
            }

            log.info("Processing premiums for portfolio ID: {}", portfolio.getId());

            // Create a map of <symbol, quantity>
            Map<String, Integer> symbolQuantityMap = portfolio.getHoldings().stream()
                    .collect(Collectors.toMap(
                            holding -> holding.getAsset().getSymbol(),
                            holding -> holding.getQuantity()
                    ));

            if (symbolQuantityMap.isEmpty()) {
                log.info("No symbols found in portfolio for user: {}", username);
                return 0.0;
            }

            log.info("Requesting premiums for symbols: {}", symbolQuantityMap.keySet());

            // Fetch suggested premiums and calculate the total weighted sum
            Double totalPremium = getSuggestedPremiums(new ArrayList<>(symbolQuantityMap.keySet()))
                    .map(premiumMap -> calculateTotalPremium(symbolQuantityMap, premiumMap))
                    .defaultIfEmpty(0.0)
                    .block();

            return totalPremium != null ? totalPremium : 0.0;

        } catch (Exception e) {
            log.error("Error calculating total premiums for user {}: {}", username, e.getMessage());
            return 0.0;
        }
    }

    private double calculateTotalPremium(Map<String, Integer> symbolQuantityMap, Map<String, Double> premiumMap) {
        return symbolQuantityMap.entrySet().stream()
                .mapToDouble(entry -> {
                    String symbol = entry.getKey();
                    int quantity = entry.getValue();
                    Double price = premiumMap.getOrDefault(symbol, 0.0);
                    return quantity * price;
                })
                .sum();
    }
}
