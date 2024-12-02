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
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.AssetNotFoundException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.AssetRepository;
import tn.zeros.marketmaster.repository.GameParticipationRepository;
import tn.zeros.marketmaster.repository.MarketDataRepository;
import tn.zeros.marketmaster.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class MarketDataService {
    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);

    private final MarketDataRepository marketDataRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;
    private final GameParticipationRepository gameParticipationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ThreadPoolTaskScheduler scheduler;

    private final Map<String, LocalDateTime> userTimestamps = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> userTasks = new ConcurrentHashMap<>();

    public void startStreaming(MarketDataRequestDto request) {
        logger.info("Received streaming request: gameId={}, assetSymbol={}, updateRate={}, username={}",
                request.getGameId(), request.getAssetSymbol(), request.getUpdateRate(), request.getUsername());

        // Log the request details
        logger.debug("Request Details: gameId={}, assetSymbol={}, updateRate={}, username={}",
                request.getGameId(), request.getAssetSymbol(), request.getUpdateRate(), request.getUsername());

        Asset asset = assetRepository.findBySymbol(request.getAssetSymbol());
        if (asset == null) {
            logger.error("Asset not found for symbol: {}", request.getAssetSymbol());
            throw new AssetNotFoundException(request.getAssetSymbol());
        }
        logger.info("Asset found: {}", asset);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    logger.error("User not found: {}", request.getUsername());
                    return new UserNotFoundException("User not found: " + request.getUsername());
                });
        logger.info("User found: {}", user);

        // Determine startTimestamp based on lastPauseTimestamp or the first market data record
        LocalDateTime lastPauseTimestamp = gameParticipationRepository.findLastPauseTimestamp(user, request.getGameId());
        logger.debug("Last pause timestamp for user {}: {}", request.getUsername(), lastPauseTimestamp);

        LocalDateTime startTimestamp = (lastPauseTimestamp != null)
                ? lastPauseTimestamp
                : marketDataRepository.findTopByGameIdAndAssetIdOrderByTimestampAsc(
                request.getGameId(), asset.getId()).getTimestamp();

        logger.info("Start timestamp for user {}: {}", request.getUsername(), startTimestamp);

        userTimestamps.put(request.getUsername(), startTimestamp);

        // Cancel any previous task if exists
        ScheduledFuture<?> previousTask = userTasks.get(request.getUsername());
        if (previousTask != null) {
            logger.info("Cancelling previous task for user {}", request.getUsername());
            previousTask.cancel(true);
        }

        // Schedule new streaming task
        logger.info("Scheduling new streaming task for user {}", request.getUsername());
        ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.debug("Fetching next market data for user {}", request.getUsername());
                MarketData marketData = fetchNextMarketData(
                        request.getGameId(), asset.getId(), userTimestamps.get(request.getUsername()));

                if (marketData != null) {
                    logger.info("New market data available for user {}: {}", request.getUsername(), marketData.toString());
                    userTimestamps.put(request.getUsername(), marketData.getTimestamp());
                    messagingTemplate.convertAndSendToUser(
                            request.getUsername(), "/queue/market-data", new MarketDataStreamDto(marketData));
                } else {
                    logger.warn("No market data available for user {} after timestamp {}",
                            request.getUsername(), userTimestamps.get(request.getUsername()));
                }
            } catch (Exception e) {
                logger.error("Error in streaming for user {}: {}", request.getUsername(), e.getMessage(), e);
                stopStreaming(request.getUsername());
            }
        }, request.getUpdateRate() * 1000L);

        userTasks.put(request.getUsername(), scheduledTask);
        logger.info("Streaming task scheduled for user {}", request.getUsername());
    }

    private MarketData fetchNextMarketData(Long gameId, Long assetId, LocalDateTime lastTimestamp) {
        logger.debug("Fetching next market data for gameId={}, assetId={}, lastTimestamp={}",
                gameId, assetId, lastTimestamp);
        return marketDataRepository
                .findFirstByGameIdAndAssetIdAndTimestampGreaterThanOrderByTimestampAsc(gameId, assetId, lastTimestamp)
                .orElse(null);
    }

    public void stopStreaming(String username) {
        ScheduledFuture<?> task = userTasks.get(username);
        if (task != null && !task.isCancelled()) {
            logger.info("Stopping market data streaming for user: {}", username);
            task.cancel(true);
            userTasks.remove(username);
        }
    }
}
