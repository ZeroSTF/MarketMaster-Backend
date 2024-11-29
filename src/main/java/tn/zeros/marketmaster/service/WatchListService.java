package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.dto.PageResponseDTO;
import tn.zeros.marketmaster.dto.RegisterAssetsRequest;
import tn.zeros.marketmaster.dto.WatchListDTO;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.UserWatchlist;
import tn.zeros.marketmaster.exception.FlaskServiceRegistrationException;
import tn.zeros.marketmaster.repository.UserWatchlistRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WatchListService {
    private final UserWatchlistRepository watchlistRepository;
    private final WebClient webClient;

    public PageResponseDTO<WatchListDTO> getWatchlistByUser(String username, int page, int size) {
        // Fetch paginated watchlist entries for the user
        Page<UserWatchlist> watchlistPage = watchlistRepository.findByUser_Username(username, PageRequest.of(page, size));

        // Map UserWatchlist entities to DTOs
        List<WatchListDTO> watchlistDTOs = watchlistPage.stream()
                .map(WatchListDTO::fromEntity)
                .collect(Collectors.toList());

        // Extract asset symbols for Flask service registration
        List<String> assetSymbols = watchlistPage.stream()
                .map(userWatchlist -> userWatchlist.getAsset().getSymbol())
                .collect(Collectors.toList());

        // Register assets with Flask service asynchronously
        registerAssetsWithFlask(assetSymbols)
                .doOnTerminate(() -> log.info("Asset registration with Flask service completed"))
                .subscribe(
                        success -> log.info("Assets registered successfully with Flask service"),
                        error -> log.error("Failed to register assets with Flask service", error)
                );

        // Return paginated response with watchlist DTOs
        return new PageResponseDTO<>(
                watchlistDTOs,
                watchlistPage.getNumber(),
                watchlistPage.getSize(),
                watchlistPage.getTotalElements(),
                watchlistPage.getTotalPages(),
                watchlistPage.isLast()
        );
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
