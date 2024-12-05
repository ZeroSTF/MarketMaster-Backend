package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.dto.AssetDTO;
import tn.zeros.marketmaster.dto.PageResponseDTO;
import tn.zeros.marketmaster.dto.RegisterAssetsRequest;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.exception.AssetFetchException;
import tn.zeros.marketmaster.exception.FlaskServiceRegistrationException;
import tn.zeros.marketmaster.repository.AssetRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final WebClient webClient;

    public PageResponseDTO<AssetDTO> getAllAssets(Integer page, Integer size) {
        try {
            Page<Asset> assetPage = findAll(PageRequest.of(page, size));

            List<String> symbols = assetPage.getContent().stream()
                    .map(Asset::getSymbol)
                    .collect(Collectors.toList());

            registerAssetsWithFlask(symbols)
                    .subscribe(
                            success -> log.debug("Assets registered successfully with Flask service"),
                            error -> log.error("Failed to register assets with Flask service", error)
                    );

            return new PageResponseDTO<>(
                    assetPage.getContent().stream()
                            .map(AssetDTO::fromEntity)
                            .collect(Collectors.toList()),
                    assetPage.getNumber(),
                    assetPage.getSize(),
                    assetPage.getTotalElements(),
                    assetPage.getTotalPages(),
                    assetPage.isLast()
            );
        } catch (Exception e) {
            log.error("Error fetching assets", e);
            throw new AssetFetchException("Failed to fetch assets", e);
        }
    }

    private Page<Asset> findAll(PageRequest pageRequest) {
        try {
            return assetRepository.findAll(pageRequest);
        } catch (Exception e) {
            log.error("Error fetching assets with pagination: {}", e.getMessage());
            throw new AssetFetchException("Failed to fetch assets", e);
        }
    }

    public Mono<Void> registerAssetsWithFlask(List<String> symbols) {
        return webClient.post()
                .uri("/api/assets/register")
                .bodyValue(new RegisterAssetsRequest(symbols))
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        response -> Mono.error(new FlaskServiceRegistrationException("Flask service registration failed"))
                )
                .bodyToMono(Void.class);
    }

    public double getCurrentPrice(Long assetId) {return 450; //TODO
        }

    public Double fetchCurrentPrice(String symbol) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/test/fetch/{symbol}").build(symbol))
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorResponse -> {
                                    log.error("Error response from Flask: {}", errorResponse);
                                    return Mono.error(new FlaskServiceRegistrationException("Flask service failed: " + errorResponse));
                                })
                )
                .bodyToMono(Map.class) // Deserialize response as a Map
                .map(responseMap -> {
                    // Extract 'data' field and then 'currentPrice'
                    Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                    if (data != null && data.containsKey("currentPrice")) {
                        Object currentPriceValue = data.get("currentPrice");
                        if (currentPriceValue instanceof Number) {
                            return ((Number) currentPriceValue).doubleValue();
                        } else {
                            throw new IllegalArgumentException("Invalid currentPrice format");
                        }
                    } else {
                        throw new IllegalArgumentException("Response missing 'currentPrice' key in 'data'");
                    }
                })
                .doOnSubscribe(subscription ->
                        log.info("Sending request to Flask API for symbol: {}", symbol))
                .doOnSuccess(result ->
                        log.info("Successfully received currentPrice for symbol: {}, price: {}", symbol, result))
                .doOnError(error ->
                        log.error("Error calling Flask API: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.warn("Returning default value due to error for symbol: {}", symbol);
                    return Mono.just(0.0); // Return a default value in case of error
                })
                .block(); // Make the call synchronous
    }



}