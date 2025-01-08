package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.dto.AssetDTO;
import tn.zeros.marketmaster.dto.PageResponseDTO;
import tn.zeros.marketmaster.dto.RegisterAssetsRequest;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.UserWatchlist;
import tn.zeros.marketmaster.exception.AssetFetchException;
import tn.zeros.marketmaster.exception.FlaskServiceRegistrationException;
import tn.zeros.marketmaster.repository.UserWatchlistRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WatchListService {
    private final UserWatchlistRepository watchlistRepository;
    private final WebClient webClient;

    public PageResponseDTO<AssetDTO> getUserWatchlist(String username, Integer page, Integer size) {
        try {
            // 1. Get paginated watchlist items from database
            Page<UserWatchlist> watchlistPage = findByUsername(username, PageRequest.of(page, size));
            List<String> symbols = watchlistPage.getContent().stream()
                    .map(watchlist -> watchlist.getAsset().getSymbol())
                    .collect(Collectors.toList());

            // 2. Register assets with Flask service
            registerAssetsWithFlask(symbols)
                    .doOnError(error -> log.error("Failed to register watchlist assets with Flask service", error))
                    .subscribe();

            // 3. Fetch current asset data from Flask
            Map<String, Map<String, Object>> flaskData = fetchAssetsDataFromFlask();

            // 4. Combine database and Flask data
            List<AssetDTO> enrichedAssets = watchlistPage.getContent().stream()
                    .map(watchlist -> enrichAssetWithFlaskData(watchlist.getAsset(), flaskData.get(watchlist.getAsset().getSymbol())))
                    .collect(Collectors.toList());

            return new PageResponseDTO<>(
                    enrichedAssets,
                    watchlistPage.getNumber(),
                    watchlistPage.getSize(),
                    watchlistPage.getTotalElements(),
                    watchlistPage.getTotalPages(),
                    watchlistPage.isLast()
            );
        } catch (Exception e) {
            log.error("Error fetching watchlist for user: {}", username, e);
            throw new AssetFetchException("Failed to fetch watchlist", e);
        }
    }

    private Page<UserWatchlist> findByUsername(String username, PageRequest pageRequest) {
        try {
            return watchlistRepository.findByUser_Username(username, pageRequest);
        } catch (Exception e) {
            log.error("Error fetching watchlist with pagination for user {}: {}", username, e.getMessage());
            throw new AssetFetchException("Failed to fetch watchlist", e);
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
                    .openPrice(parseDouble(flaskData.get("openPrice")))
                    .dayHigh(parseDouble(flaskData.get("dayHigh")))
                    .dayLow(parseDouble(flaskData.get("dayLow")))
                    .averageVolume(parseDouble(flaskData.get("averageVolume")))
                    .currentPrice(parseDouble(flaskData.get("currentPrice")))
                    .volume(parseLong(flaskData.get("volume")))
                    .previousClose(parseDouble(flaskData.get("previousClose")))
                    .priceChange(parseDouble(flaskData.get("priceChange")))
                    .priceChangePercent(parseDouble(flaskData.get("priceChangePercent")))
                    .marketCap(parseDouble(flaskData.get("marketCap")))
                    .peRatio(parseDouble(flaskData.get("peRatio")))
                    .dividendYieldPercent(parseDouble(flaskData.get("dividendYieldPercent")))
                    .beta(parseDouble(flaskData.get("beta")))
                    .yearHigh(parseDouble(flaskData.get("yearHigh")))
                    .yearLow(parseDouble(flaskData.get("yearLow")))
                    .sector(flaskData.get("sector").toString());
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

    private Mono<Void> registerAssetsWithFlask(List<String> symbols) {
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
}
