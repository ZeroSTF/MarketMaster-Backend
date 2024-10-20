package tn.zeros.marketmaster.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tn.zeros.marketmaster.dto.AssetDailyDto;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AlphaVantageService {

    @Value("${alpha.key}")
    private String API_KEY;
    private static final String BASE_URL = "https://www.alphavantage.co/query";
    @Autowired
    private  SimpMessagingTemplate messagingTemplate;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private static final List<String> SYMBOLS = List.of(
            "AAPL", "MSFT", "GOOGL", "AMZN", "JNJ", "TSLA",
            "AAPL", "MSFT", "GOOGL", "AMZN", "JNJ", "TSLA",
            "AAPL", "MSFT", "GOOGL", "AMZN", "JNJ", "TSLA"
    );
    public String getStockData(String symbol) {
        // Build the URL for the Alpha Vantage API request
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("function", "GLOBAL_QUOTE")
                .queryParam("symbol", symbol)
                .queryParam("apikey", API_KEY);



        // You can return the response as a String or parse it into a specific object model
        return restTemplate.getForObject(uriBuilder.toUriString(), String.class);
    }
    private AssetDailyDto parseResponse(String response) {
        try {
            // Parse the JSON string
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode quoteNode = rootNode.path("Global Quote");

            // Create and populate AssetDailyDto
            AssetDailyDto assetDailyDto = new AssetDailyDto();
            assetDailyDto.setSymbol(quoteNode.path("01. symbol").asText());
            assetDailyDto.setOpen(quoteNode.path("02. open").asDouble());
            assetDailyDto.setHigh(quoteNode.path("03. high").asDouble());
            assetDailyDto.setLow(quoteNode.path("04. low").asDouble());
            assetDailyDto.setPrice(quoteNode.path("05. price").asDouble());
            assetDailyDto.setVolume(quoteNode.path("06. volume").asLong());
            assetDailyDto.setLatestTradingDay(quoteNode.path("07. latest trading day").asText());
            assetDailyDto.setPreviousClose(quoteNode.path("08. previous close").asDouble());
            assetDailyDto.setChange(quoteNode.path("09. change").asDouble());
            assetDailyDto.setChangePercent(quoteNode.path("10. change percent").asText());

            return assetDailyDto;
        } catch (Exception e) {
            // Handle the exception (e.g., log it)
            e.printStackTrace();
            return null; // Or throw a custom exception
        }
    }
    public List<AssetDailyDto> dailyDtos(){
        List<AssetDailyDto> dailyDtoList =new ArrayList<>();
        for (String symbol : SYMBOLS){
            log.info("symbol to send :" +symbol);
            String response = this.getStockData(symbol);
            log.info("data from api  :" +response);
            AssetDailyDto dailyDto = this.parseResponse(response);
            dailyDtoList.add(dailyDto);
        }
        return dailyDtoList;
    }
  /*  @Scheduled(fixedRate = 60000) // Fetch stock data every 10 seconds
    public void sendStockUpdates() {
        List<AssetDailyDto> dailyDto = this.dailyDtos();
        for (AssetDailyDto dto:dailyDto){
            log.info("daily  dto : "+dto.getSymbol());
        }
        messagingTemplate.convertAndSend("/topic/market", dailyDto);
    }*/
}

