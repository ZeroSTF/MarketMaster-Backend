package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.zeros.marketmaster.dto.AssetStatisticsDto;
import tn.zeros.marketmaster.service.YfinaceService;

@RestController
@RequestMapping("/yfinance")
@RequiredArgsConstructor
public class YFinanceController {
    private final YfinaceService yfinaceService;
    @GetMapping("/stats")
    public ResponseEntity<AssetStatisticsDto> getStats(@RequestParam String symbol) {
        AssetStatisticsDto stats = yfinaceService.getStockStatistics(symbol);
        if (stats != null) {
            return ResponseEntity.ok(stats);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
