package tn.zeros.marketmaster.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.service.PortfolioService;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portf")
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;


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


    @PutMapping("/{id}")
    public ResponseEntity<PortfolioDTO> updatePortfolio(@PathVariable Long id, @RequestBody PortfolioDTO portfolioDTO) {
        try {
            portfolioDTO.setId(id);
            PortfolioDTO updatedPortfolio = portfolioService.updatePortfolio(portfolioDTO);
            return ResponseEntity.ok(updatedPortfolio);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
/*
    @GetMapping("/{id}/holding-value")
    public ResponseEntity<Double> getPortfolioHoldingValue(@PathVariable Long id) {
        try {
            double value = portfolioService.calculatePortfolioHolding(id);
            return ResponseEntity.ok(value);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/gain-loss")
    public ResponseEntity<Double> getPortfolioGainLoss(
            @PathVariable Long id,
            @RequestParam Duration duration) {
        try {
            double gainLoss = portfolioService.calculateGainLoss(id, duration);
            return ResponseEntity.ok(gainLoss);
        } catch (PortfolioNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
*/
}
