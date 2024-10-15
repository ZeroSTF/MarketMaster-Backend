package tn.zeros.marketmaster.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AlphaVantageService {

    private static final String API_KEY = "X63Y5H1V3K80OUS9";
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    @Autowired
    private RestTemplate restTemplate;

    public String getStockData(String symbol) {
        // Build the URL for the Alpha Vantage API request
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                .queryParam("function", "TIME_SERIES_INTRADAY")
                .queryParam("symbol", symbol)
                .queryParam("interval", "5min")
                .queryParam("apikey", API_KEY);

        // Make the request to the Alpha Vantage API
        String response = restTemplate.getForObject(uriBuilder.toUriString(), String.class);

        // You can return the response as a String or parse it into a specific object model
        return response;
    }
}

