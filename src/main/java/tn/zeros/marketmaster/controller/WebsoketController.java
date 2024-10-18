package tn.zeros.marketmaster.controller;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
@AllArgsConstructor

public class WebsoketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate;



    @MessageMapping("/fetchStockData") // WebSocket endpoint to fetch stock data
    public void fetchStockData(String symbol) {
        // Call the REST endpoint to get stock data
        String stockData = restTemplate.getForObject("http://localhost:8081/alpha/stock-data?symbol=" + symbol, String.class);

        // Send the retrieved stock data to all subscribed clients
        messagingTemplate.convertAndSend("/topic/market", stockData);
    }
}
