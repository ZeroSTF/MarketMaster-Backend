package tn.zeros.marketmaster.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.HoldingDTO;
import tn.zeros.marketmaster.dto.OverviewDTO;
import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
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

    @PostMapping("new/{userName}")
    public ResponseEntity<PortfolioDTO> createNewPortfolio(@PathVariable String userName) {

        try {
            PortfolioDTO newPortfolio = portfolioService.newPortfolio(userName);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPortfolio);
        } catch (UsernameNotFoundException e) {

            return ResponseEntity.notFound().build();
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/overview/{userName}")
    public ResponseEntity<OverviewDTO> getOverviewData(@PathVariable("userName") String userName) {
        try {
            OverviewDTO overviewData = portfolioService.prepareOverview(userName);
            return ResponseEntity.ok(overviewData);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/totalVal/{userName}")
    public ResponseEntity<List<Map<LocalDateTime, Double>>> totalVal(@PathVariable("userName") String userName) {
        try {
            List<Map<LocalDateTime, Double>> totalValues = portfolioService.getTotalValueByPortfolioId(userName);
            return ResponseEntity.ok(totalValues);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/holding/{userName}")
    public ResponseEntity<List<HoldingDTO>> getHoldingData(@PathVariable("userName") String userName) {
        try {
            List<HoldingDTO> holdingData = holdingService.getAll(userName);
            return ResponseEntity.ok(holdingData);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
