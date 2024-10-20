package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetDataService {
    private static final String YFINANCE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private final RestTemplate restTemplate;
    private final CacheManager cacheManager;

    @Cacheable(value = "assetData", key = "#symbol", unless = "#result == null")
    public String getAssetData(String symbol) {
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(YFINANCE_URL + symbol)
                    .queryParam("interval", "1d")
                    .queryParam("range", "1d");

            return restTemplate.getForObject(uriBuilder.toUriString(), String.class);
        } catch (Exception e) {
            log.error("Failed to fetch asset data for symbol: {} - {}", symbol, e.getMessage());
            return null;
        }
    }

    @Scheduled(fixedRateString = "${asset.cache.duration}")
    public void evictCache() {
        Cache cache = cacheManager.getCache("assetData");
        if (cache != null) {
            cache.clear();
            log.debug("Asset data cache cleared");
        }
    }
}