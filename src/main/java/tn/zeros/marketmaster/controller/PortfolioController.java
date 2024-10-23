package tn.zeros.marketmaster.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.HoldingDTO;
import tn.zeros.marketmaster.dto.OverviewDTO;
import tn.zeros.marketmaster.service.HoldingService;
import tn.zeros.marketmaster.service.PortfolioService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portf")
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final HoldingService holdingService;


    @GetMapping("/overview/{userName}")
    public ResponseEntity<OverviewDTO> getOverviewData(@PathVariable("userName") String username) {
        OverviewDTO overviewData = portfolioService.prepareOverview(username);
        return ResponseEntity.ok(overviewData);
    }

    @GetMapping("/totalVal/{userName}")
    public ResponseEntity<List<Map<LocalDateTime, Double>>> totalVal(@PathVariable("userName") String username) {
        List<Map<LocalDateTime, Double>> totalValues = portfolioService.getTotalValueByPortfolioId(username);
        return ResponseEntity.ok(totalValues);
    }

    @GetMapping("/holding/{userName}")
    public ResponseEntity<List<HoldingDTO>> getHoldingData(@PathVariable("userName") String username) {
        List<HoldingDTO> holdingData = holdingService.getAll(username);
        return ResponseEntity.ok(holdingData);
    }
}
