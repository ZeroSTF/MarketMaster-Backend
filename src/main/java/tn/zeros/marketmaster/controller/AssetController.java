package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.dto.AssetDTO;
import tn.zeros.marketmaster.dto.PageResponseDTO;
import tn.zeros.marketmaster.entity.PredictionRequest;
import tn.zeros.marketmaster.entity.StockPredictionResponse;
import tn.zeros.marketmaster.service.AssetService;
import tn.zeros.marketmaster.service.StockPredictionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/asset")
@Slf4j
public class AssetController {
    private final AssetService assetService;
    private final StockPredictionService stockPredictionService;

    @GetMapping("/getAll")
    public ResponseEntity<PageResponseDTO<AssetDTO>> getAllAssets(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageResponseDTO<AssetDTO> response = assetService.getAllAssets(page, size);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/predict")
    public Mono<ResponseEntity<StockPredictionResponse>> predictStock(@RequestBody PredictionRequest request) {
        log.info("Predicting stock for symbol: {}, train: {}", request.getSymbol(), request.isTrain());
        return stockPredictionService.getStockPrediction(request.getSymbol(), request.isTrain())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(500).body(null));
    }

}