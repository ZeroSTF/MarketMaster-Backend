package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.service.PortfolioService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portf")
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;

    @PutMapping("/update/{userId}")
    public void updatePortfolio(@PathVariable Long userId) {

            portfolioService.updatePortfolio(userId);

    }

    @PutMapping("/new/{userId}")
    public void newPortfolio(@PathVariable Long userId) {

            portfolioService.newPortfolio(userId);
         }
}
