package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.entity.Portfolio;

import java.util.List;
import java.util.Map;
@Service
@Slf4j
@RequiredArgsConstructor
public class InsuranceService {

   //private final RestTemplate restTemplate;
   private final WebClient webClient;
   private final PortfolioService portfolioService;
    public Mono<Map<String, Double>> getSuggestedPremiums(List<String> symbols) {
        return webClient.post()
                .uri("/api/assets/premiums") // Assuming this is the endpoint in your Flask app
                .bodyValue(Map.of("symbols", symbols)) // Create a request body
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Double>>() {}) // Specify the type
                .onErrorResume(e -> {
                    // Handle error case (e.g., log it, return a default value, etc.)
                    log.error("Error calling Python API: {}", e.getMessage()); // Use logger
                    return Mono.empty();
                });
    }

    public Double getTotalPremiums(Long id) {
        Portfolio portfolio = portfolioService.getById(id);
        List<String> symbols = portfolio.getHoldings().stream()
                .map(holding -> holding.getAsset().getSymbol())
                .toList();
         for (String s : symbols){
             log.info("primes : "+s);
         }
        return getSuggestedPremiums(symbols)
                .map(this::sumPremiums)
                .block();
    }
    private Double sumPremiums(Map<String, Double> premiums) {
        return premiums.values().stream()
                .reduce(0.0, Double::sum); // Sum all premium values
    }
}
