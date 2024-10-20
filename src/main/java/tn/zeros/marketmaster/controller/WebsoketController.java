package tn.zeros.marketmaster.controller;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import tn.zeros.marketmaster.dto.AssetDailyDto;
import tn.zeros.marketmaster.service.AlphaVantageService;
import tn.zeros.marketmaster.service.YfinaceService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebsoketController {

    private final SimpMessagingTemplate messagingTemplate;

    private final RestTemplate restTemplate;


    private final YfinaceService yfinaceService;

    @MessageMapping("/fetchStockData") // WebSocket endpoint to fetch stock data
    public void fetchStockData() {
        // Call the REST endpoint to get stock data
        List<AssetDailyDto> dailyDtos = yfinaceService.dailyDtos();

        // Send the retrieved stock data to all subscribed clients
        messagingTemplate.convertAndSend("/topic/market", dailyDtos);
    }

}
