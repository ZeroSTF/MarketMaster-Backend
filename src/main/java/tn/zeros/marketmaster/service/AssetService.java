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

import java.time.LocalDateTime;
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
            // 1. Get paginated assets from database
            Page<Asset> assetPage = findAll(PageRequest.of(page, size));
            List<String> symbols = assetPage.getContent().stream()
                    .map(Asset::getSymbol)
                    .collect(Collectors.toList());

            // 2. Register assets with Flask service
            registerAssetsWithFlask(symbols)
                    .doOnError(error -> log.error("Failed to register assets with Flask service", error))
                    .subscribe();

            // 3. Fetch current asset data from Flask
            Map<String, Map<String, Object>> flaskData = fetchAssetsDataFromFlask();

            // 4. Combine database and Flask data
            List<AssetDTO> enrichedAssets = assetPage.getContent().stream()
                    .map(asset -> enrichAssetWithFlaskData(asset, flaskData.get(asset.getSymbol())))
                    .collect(Collectors.toList());

            return new PageResponseDTO<>(
                    enrichedAssets,
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

    private Map<String, Map<String, Object>> fetchAssetsDataFromFlask() {
        try {
            List<Map<String, Object>> flaskResponse = webClient.get()
                    .uri("/api/assets/data")
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            return flaskResponse.stream().collect(Collectors.toMap(
                    entry -> (String) entry.get("symbol"),
                    entry -> entry
            ));
        } catch (Exception e) {
            log.error("Failed to fetch data from Flask service", e);
            throw new FlaskServiceRegistrationException("Failed to fetch asset data from Flask", e);
        }
    }

    private AssetDTO enrichAssetWithFlaskData(Asset asset, Map<String, Object> flaskData) {
        AssetDTO.AssetDTOBuilder builder = AssetDTO.builder()
                .id(asset.getId())
                .symbol(asset.getSymbol())
                .name(asset.getName());

        if (flaskData != null) {
            builder
                    .currentPrice(parseDouble(flaskData.get("currentPrice")))
                    .change(parseDouble(flaskData.get("priceChange")))
                    .changePercent(parseDouble(flaskData.get("priceChangePercent")))
                    .volume(parseLong(flaskData.get("volume")))
                    .lastUpdated(LocalDateTime.parse((String) flaskData.get("timestamp")));
        }
        return builder.build();
    }

    private Double parseDouble(Object value) {
        try {
            return value != null ? Double.parseDouble(value.toString()) : null;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse double value: {}", value);
            return null;
        }
    }

    private Long parseLong(Object value) {
        try {
            return value != null ? Long.parseLong(value.toString()) : null;
        } catch (NumberFormatException e) {
            log.warn("Failed to parse long value: {}", value);
            return null;
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