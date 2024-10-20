package tn.zeros.marketmaster.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.AssetDiscoverDTO;
import tn.zeros.marketmaster.repository.AssetRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final AssetDataService assetDataService;

    public List<String> getAllSymbols() {
        return assetRepository.findAllSymbols();
    }

    public AssetDiscoverDTO parseResponse(String response, String symbol) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode resultNode = rootNode.path("chart").path("result").get(0);
            JsonNode quoteNode = resultNode.path("indicators").path("quote").get(0);
            JsonNode metaNode = resultNode.path("meta");

            double price = metaNode.path("regularMarketPrice").asDouble();
            double previousClose = metaNode.path("chartPreviousClose").asDouble();
            double change = price - previousClose;

            return new AssetDiscoverDTO(
                    symbol,
                    quoteNode.path("open").get(0).asDouble(),
                    quoteNode.path("high").get(0).asDouble(),
                    quoteNode.path("low").get(0).asDouble(),
                    price,
                    quoteNode.path("volume").get(0).asLong(),
                    metaNode.path("regularMarketTime").asText(),
                    previousClose,
                    change,
                    String.format("%.2f%%", (change / previousClose) * 100)
            );
        } catch (Exception e) {
            log.error("Error parsing response for symbol: {} - {}", symbol, e.getMessage());
            return null;
        }
    }

    public List<AssetDiscoverDTO> fetchDailyData() {
        return getAllSymbols().parallelStream()
                .map(symbol -> {
                    String response = assetDataService.getStockData(symbol);
                    return parseResponse(response, symbol);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRateString = "${asset.update.interval:20000}")
    public void sendStockUpdates() {
        List<AssetDiscoverDTO> dailyDto = this.fetchDailyData();
        messagingTemplate.convertAndSend("/topic/market", dailyDto);
    }

    public double getCurrentPrice(Long assetId) {
        return 80; //TODO
    }
}