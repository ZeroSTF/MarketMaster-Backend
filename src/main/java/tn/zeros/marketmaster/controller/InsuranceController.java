package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.zeros.marketmaster.service.InsuranceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/insurance")
public class InsuranceController {
    private final InsuranceService insuranceService;
    @GetMapping("/primes/{id}")
    public Double totalPrimes(@PathVariable Long id){
        return insuranceService.getTotalPremiums(id);
    }

}
