package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.PortfolioDTO;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.exception.PortfolioNotFoundException;
import tn.zeros.marketmaster.service.PortfolioService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portf")
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;

    @PutMapping("update/{userId}")
    public ResponseEntity<PortfolioDTO> updatePortfolio(@PathVariable Long userId) {
        log.info("Updating portfolio for user ID: {}", userId);
        try {
            PortfolioDTO updatedPortfolio = portfolioService.updatePortfolio(userId);
            return ResponseEntity.ok(updatedPortfolio);
        } catch (PortfolioNotFoundException e) {
            log.error("Portfolio not found for user ID: {}", userId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating portfolio for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("new/{userId}")
    public ResponseEntity<PortfolioDTO> createNewPortfolio(@PathVariable Long userId) {
        log.info("Creating new portfolio for user ID: {}", userId);
        try {
            PortfolioDTO newPortfolio = portfolioService.newPortfolio(userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(newPortfolio);
        } catch (UsernameNotFoundException e) {
            log.error("User not found with ID: {}", userId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error creating new portfolio for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
