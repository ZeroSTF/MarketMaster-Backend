package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.exception.PortfolioException;
import tn.zeros.marketmaster.service.PortfolioService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portf")
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;

    @PutMapping("/update/{userId}")
    public Portfolio updatePortfolio(@PathVariable Long userId) {

        try {
            return portfolioService.updatePortfolio(userId);
        } catch (PortfolioException e) {
            throw new PortfolioException("Portfolio not found for user ID: " + userId);
        } catch (Exception e) {
            throw new RuntimeException("Error updating portfolio for user ID: " + userId);
        }

    }

    @PutMapping("/new/{userId}")
    public Portfolio newPortfolio(@PathVariable Long userId) {

        try {
            return portfolioService.newPortfolio(userId);
        } catch (UsernameNotFoundException e) {
            throw new UsernameNotFoundException("User not found with ID: " + userId);
        } catch (Exception e) {
            throw new RuntimeException("Error creating new portfolio for user ID: " + userId);
        }
    }

}
