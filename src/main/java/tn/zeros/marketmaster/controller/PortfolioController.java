package tn.zeros.marketmaster.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tn.zeros.marketmaster.dto.*;
import tn.zeros.marketmaster.entity.Option;
import tn.zeros.marketmaster.service.HoldingService;
import tn.zeros.marketmaster.service.LimitOrderService;
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
    private final LimitOrderService limitOrderService;


    @GetMapping("/overview/{username}")
    public ResponseEntity<OverviewDTO> getOverviewData(@PathVariable("username") String username) {
        OverviewDTO overviewData = portfolioService.prepareOverview(username);
        return ResponseEntity.ok(overviewData);
    }

    @GetMapping("/totalVal/{username}")
    public ResponseEntity<List<Map<LocalDateTime, Double>>> totalVal(@PathVariable("username") String username) {
        List<Map<LocalDateTime, Double>> totalValues = portfolioService.getTotalValueByPortfolioId(username);
        return ResponseEntity.ok(totalValues);
    }

    @GetMapping("/holding/{username}")
    public ResponseEntity<List<HoldingDTO>> getHoldingData(@PathVariable("username") String username){
        List<HoldingDTO> holdingData = holdingService.getAll(username);
        return ResponseEntity.ok(holdingData);
    }

    @GetMapping("/transaction/{username}")
    public ResponseEntity<List<TransactionDTO>> getTransactionData(@PathVariable("username") String username) {
       List<TransactionDTO> transactionDTOS= portfolioService.getAllTransactions(username);
       return  ResponseEntity.ok(transactionDTOS);
    }

    @PostMapping("/addwatchlist/{username}/{symbol}")
    public ResponseEntity<WatchListDTO> addWatchList(@PathVariable("username") String username, @PathVariable("symbol") String symbol) {
        WatchListDTO watchListDTO1 = portfolioService.addWatchList(username, symbol);
        return ResponseEntity.ok(watchListDTO1);
    }

    @GetMapping("/watchlist/{username}")
    public ResponseEntity<List<WatchListDTO>> getWatchList(@PathVariable("username") String username) {
        List<WatchListDTO> watchListDTOS = portfolioService.getAllWatchList(username);
        return ResponseEntity.ok(watchListDTOS);
    }

    @GetMapping("/totalValues/{username}")
    public List<Map.Entry<LocalDateTime, Double>> getTotalValues(@PathVariable String username) {
        return portfolioService.getTotalValues(username);
    }

    @GetMapping("/bestWinner")
    public List<BestWinnerDTO> getBestWinners(){
        return portfolioService.getBestWinners();
    }

    @GetMapping("/LimitOrder/{username}")
    public List<LimitOrderDTO> getLimitOrders(@PathVariable String username){
        return limitOrderService.getAllLimitOrders(username);
    }
    @GetMapping("/myoptions/{username}")
    public  List<Option> getOptions(@PathVariable String username) {
        return portfolioService.getAllOptions(username);
    }

    @GetMapping("/performances/{username}")
    public Mono<List<AssetPerformance>> getPortfolioPerformances(@PathVariable String username) {
        return portfolioService.getPortfolioPerformances(username);
    }

    @GetMapping("/correlation/{username}")
    public Mono<Map<String, Map<String, Double>>> getPortfolioCorrelationMatrix(@PathVariable String username) {
        return portfolioService.getPortfolioCorrelationMatrix(username);
    }
}
