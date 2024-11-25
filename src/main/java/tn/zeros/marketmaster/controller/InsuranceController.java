package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.service.InsuranceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/insurance")
public class InsuranceController {
    private final InsuranceService insuranceService;

    @GetMapping("/primes/{username}")
    public Double totalPrimes(@PathVariable String username) {
        // Directly return the Mono from the service method.
        return insuranceService.getTotalPremiums(username);

    }
}
