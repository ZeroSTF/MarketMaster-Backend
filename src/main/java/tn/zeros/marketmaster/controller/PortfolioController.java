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
@RequestMapping("/portf")
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final HoldingService holdingService;

    @PostMapping("new/{userId}")
    public ResponseEntity<PortfolioDTO> createNewPortfolio(@PathVariable Long userId) {

        try {
            PortfolioDTO newPortfolio = portfolioService.newPortfolio(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPortfolio);
        } catch (UsernameNotFoundException e) {

            return ResponseEntity.notFound().build();
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
     * @PutMapping("/{id}")
     * public ResponseEntity<PortfolioDTO> updatePortfolio(@PathVariable Long
     * id, @RequestBody PortfolioDTO portfolioDTO) {
     * try {
     * portfolioDTO.setId(id);
     * PortfolioDTO updatedPortfolio =
     * portfolioService.updatePortfolio(portfolioDTO);
     * return ResponseEntity.ok(updatedPortfolio);
     * } catch (PortfolioNotFoundException e) {
     * return ResponseEntity.notFound().build();
     * } catch (EntityNotFoundException e) {
     * return ResponseEntity.badRequest().build();
     * }
     * }
     */

    @PostMapping("/overview/{id}")
    public ResponseEntity<OverviewDTO> getOverviewData(@PathVariable("id") Long id) {
        try {
            OverviewDTO overviewData = portfolioService.prepareOverview(id);
            return ResponseEntity.ok(overviewData);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/totalVal/{id}")
    public ResponseEntity<List<Map<LocalDateTime, Double>>> totalVal(@PathVariable("id") Long id) {
        try {
            List<Map<LocalDateTime, Double>> totalValues = portfolioService.getTotalValueByPortfolioId(id);
            return ResponseEntity.ok(totalValues);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/holding/{id}")
    public ResponseEntity<List<HoldingDTO>> getHoldingData(@PathVariable("id") Long id) {
        try {
            List<HoldingDTO> holdingData = holdingService.getAll(id);
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
