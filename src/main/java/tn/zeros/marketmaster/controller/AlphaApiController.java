package tn.zeros.marketmaster.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.service.AlphaVantageService;

@RestController
@RequestMapping("/alpha")

public class AlphaApiController {
    @Autowired
    private AlphaVantageService alphaVantageService;

    @GetMapping("/stock-data")
    public String getStockData(@RequestParam String symbol) {
        return alphaVantageService.getStockData(symbol);
    }
}
