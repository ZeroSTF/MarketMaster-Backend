package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.dto.AssetPerformance;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockPredictionService {

    private final WebClient webClient;


    public Mono<List<AssetPerformance>> getAssetPerformances(List<String> symbols) {
        return webClient.post()
                .uri(  "/api/performance")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("symbols", symbols)) // Send symbols in the body
                .retrieve()
                .bodyToFlux(AssetPerformance.class)
                .collectList() // Convert Flux to List
                .onErrorResume(e -> {

                    System.err.println("Error fetching asset performances: " + e.getMessage());
                    return Mono.just(List.of());
                });
    }
    public Mono<Map<String, Map<String, Double>>> getCorrelationMatrix(List<String> symbols) {
        System.out.println("Sending symbols to Flask API: " + symbols);
        return webClient.post()
                .uri("/api/correlation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Collections.singletonMap("symbols", symbols))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Map<String, Double>>>() {})
                .doOnNext(response -> System.out.println("Received response from Flask API: " + response))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(Collections.emptyMap());
                });
    }
}
