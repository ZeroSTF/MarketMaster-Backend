package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.MarketDataRequestDto;
import tn.zeros.marketmaster.service.MarketDataService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    @MessageMapping("/market-data/request")
    public void handleMarketDataRequest(MarketDataRequestDto request) {
        System.out.println("Requested by username: " + request.getUsername());
        marketDataService.startStreaming(request);
    }


    @MessageMapping("/market-data/stop")
    public void handleMarketDataStop(String username) {
        marketDataService.stopStreaming(username);
    }
}
