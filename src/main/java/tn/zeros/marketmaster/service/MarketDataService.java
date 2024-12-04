package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.MarketDataRequestDto;
import tn.zeros.marketmaster.dto.MarketDataStreamDto;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.MarketData;
import tn.zeros.marketmaster.exception.AssetNotFoundException;
import tn.zeros.marketmaster.repository.AssetRepository;
import tn.zeros.marketmaster.repository.MarketDataRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class MarketDataService {
    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);  // Initialize the logger

    private final MarketDataRepository marketDataRepository;
    private final AssetRepository assetRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private ScheduledFuture<?> scheduledTask;

    // Track the timestamp of the last data sent
    private LocalDateTime lastTimestampSent = null;

    public void startStreaming(MarketDataRequestDto request) {
        logger.info("Received request for streaming market data: gameId={}, assetSymbol={}, updateRate={}",
                request.getGameId(), request.getAssetSymbol(), request.getUpdateRate());

        Asset asset;
        try {
            logger.debug("Attempting to fetch asset with symbol: {}", request.getAssetSymbol());
            asset = assetRepository.findBySymbol(request.getAssetSymbol());
            if (asset == null) {
                logger.error("Asset not found for symbol: {}", request.getAssetSymbol());
                throw new AssetNotFoundException(request.getAssetSymbol());
            }
            logger.info("Asset found: {}", asset);
        } catch (Exception e) {
            logger.error("Error fetching asset for symbol: {}", request.getAssetSymbol(), e);
            throw new AssetNotFoundException(request.getAssetSymbol());
        }

        Long assetId = asset.getId();
        scheduler.setPoolSize(1);
        scheduler.initialize();

        // Cancel any previous scheduled task before scheduling a new one
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            logger.info("Cancelling previous scheduled task.");
            scheduledTask.cancel(true);
        }

        logger.info("Scheduling new market data streaming task with update rate of {} seconds.", request.getUpdateRate());

        scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.debug("Fetching market data for gameId={} and assetId={}, starting after timestamp={}",
                        request.getGameId(), assetId, lastTimestampSent);

                MarketData marketData;
                if (lastTimestampSent != null) {
                    Optional<MarketData> optionalMarketData = marketDataRepository
                            .findFirstByGameIdAndAssetIdAndTimestampGreaterThanOrderByTimestampAsc(
                                    request.getGameId(), assetId, lastTimestampSent);
                    marketData = optionalMarketData.orElse(null);
                } else {
                    marketData = marketDataRepository.findTopByGameIdAndAssetIdOrderByTimestampAsc(
                            request.getGameId(), assetId);
                }

                if (marketData != null) {
                    lastTimestampSent = marketData.getTimestamp();  // Update the last sent timestamp
                    MarketDataStreamDto marketDataDto = new MarketDataStreamDto(marketData);
                    logger.info("Sending market data over WebSocket: {}", marketDataDto);
                    messagingTemplate.convertAndSend("/topic/market-data", marketDataDto);
                } else {
                    logger.warn("No new market data found for gameId={} and assetId={} after timestamp={}",
                            request.getGameId(), assetId, lastTimestampSent);
                }
            } catch (Exception e) {
                logger.error("Error during market data streaming for gameId={} and assetId={}",
                        request.getGameId(), assetId, e);
            }
        }, request.getUpdateRate() * 1000L);  // Update rate in milliseconds
    }
}
