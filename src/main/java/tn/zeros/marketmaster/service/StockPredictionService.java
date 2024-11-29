package tn.zeros.marketmaster.service;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.entity.StockPredictionResponse;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class StockPredictionService {

    private final WebClient webClient;

    public Mono<StockPredictionResponse> getStockPrediction(String symbol, boolean train) {
        String payload = "{ \"train\": " + train + " }";  // Only sending 'train' in the body

        return webClient.post()
                .uri("/api/predict/{symbol}", symbol)  // Use the 'symbol' as a path variable
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(StockPredictionResponse.class)
                .onErrorResume(e -> {
                    // Handle error and return a response with an error message
                    return Mono.just(new StockPredictionResponse(
                            0.0,  // Placeholder values
                            "Error",
                            0.0,
                            0.0
                    ));
                });
    }
}
