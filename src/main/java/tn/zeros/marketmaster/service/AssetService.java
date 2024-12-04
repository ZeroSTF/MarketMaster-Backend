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
import tn.zeros.marketmaster.exception.AssetFetchException;
import tn.zeros.marketmaster.exception.FlaskServiceRegistrationException;
import tn.zeros.marketmaster.repository.AssetRepository;

import java.util.List;
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

    public double getCurrentPrice(Long assetId) {
        return 450; //TODO
    }
}