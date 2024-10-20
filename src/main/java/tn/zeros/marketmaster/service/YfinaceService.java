package tn.zeros.marketmaster.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tn.zeros.marketmaster.dto.AssetDailyDto;
import tn.zeros.marketmaster.dto.AssetStatisticsDto;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class YfinaceService {

private static final String BASE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String URL_STATS = "https://query2.finance.yahoo.com/v10/finance/quoteSummary/";
    private static final String CRUMB_URL = "https://query1.finance.yahoo.com/v1/test/getcrumb";
    private static final String COOKIE_URL = "https://fc.yahoo.com";
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final List<String> SYMBOLS = List.of(
            // Technology
            "AAPL",  // Apple Inc.
            "MSFT",  // Microsoft Corporation
            "GOOGL", // Alphabet Inc. (Google)
            "AMZN",  // Amazon.com Inc.
            "TSLA",  // Tesla Inc.
            "NVDA",  // NVIDIA Corporation
            "INTC",  // Intel Corporation
            "AMD",   // Advanced Micro Devices Inc.
            "CSCO",  // Cisco Systems Inc.
            "IBM",   // International Business Machines Corporation

            // Healthcare
            "JNJ",   // Johnson & Johnson
            "PFE",   // Pfizer Inc.
            "UNH",   // UnitedHealth Group Incorporated
            "ABBV",  // AbbVie Inc.
            "MRK",   // Merck & Co. Inc.

            // Finance
            "JPM",   // JPMorgan Chase & Co.
            "BAC",   // Bank of America Corporation
            "WFC",   // Wells Fargo & Company
            "V",     // Visa Inc.
            "MA",    // Mastercard Incorporated

            // Consumer Goods
            "PG",    // The Procter & Gamble Company
            "KO",    // The Coca-Cola Company
            "PEP",   // PepsiCo Inc.
            "WMT",   // Walmart Inc.
            "COST",  // Costco Wholesale Corporation

            // Industrial
            "GE",    // General Electric Company
            "BA",    // The Boeing Company
            "CAT",   // Caterpillar Inc.
            "MMM",   // 3M Company
            "HON"    // Honeywell International Inc.
    );

    public String getStockData(String symbol) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(BASE_URL + symbol)
                .queryParam("interval", "1d")
                .queryParam("range", "1d");

        return restTemplate.getForObject(uriBuilder.toUriString(), String.class);
    }

    public AssetStatisticsDto getStockStatistics(String symbol) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(URL_STATS + symbol)
                .queryParam("modules", "defaultKeyStatistics,financialData,summaryDetail");
        String response = restTemplate.getForObject(uriBuilder.toUriString(), String.class);
        log.info("stats data : "+response);
        AssetStatisticsDto statisticsDto= this.parseResponseStat(response,symbol);
        return statisticsDto;
    }
    private AssetDailyDto parseResponse(String response, String symbol) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode resultNode = rootNode.path("chart").path("result").get(0);
            JsonNode quoteNode = resultNode.path("indicators").path("quote").get(0);
            JsonNode metaNode = resultNode.path("meta");

            AssetDailyDto assetDailyDto = new AssetDailyDto();
            assetDailyDto.setSymbol(symbol);
            assetDailyDto.setOpen(quoteNode.path("open").get(0).asDouble());
            assetDailyDto.setHigh(quoteNode.path("high").get(0).asDouble());
            assetDailyDto.setLow(quoteNode.path("low").get(0).asDouble());
            assetDailyDto.setPrice(metaNode.path("regularMarketPrice").asDouble());
            assetDailyDto.setVolume(quoteNode.path("volume").get(0).asLong());
            assetDailyDto.setLatestTradingDay(metaNode.path("regularMarketTime").asText());
            assetDailyDto.setPreviousClose(metaNode.path("chartPreviousClose").asDouble());

            double change = assetDailyDto.getPrice() - assetDailyDto.getPreviousClose();
            assetDailyDto.setChange(change);

            double changePercent = (change / assetDailyDto.getPreviousClose()) * 100;
            assetDailyDto.setChangePercent(String.format("%.2f%%", changePercent));

            return assetDailyDto;
        } catch (Exception e) {
            log.error("Error parsing response for symbol: " + symbol, e);
            return null;
        }
    }
    public AssetStatisticsDto parseResponseStat(String response, String symbol) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode resultNode = rootNode.path("quoteSummary").path("result").get(0);
            JsonNode defaultKeyStatistics = resultNode.path("defaultKeyStatistics");
            JsonNode financialData = resultNode.path("financialData");
            JsonNode summaryDetail = resultNode.path("summaryDetail");

            AssetStatisticsDto assetStatisticsDto = new AssetStatisticsDto();
            assetStatisticsDto.setSymbol(symbol);
            assetStatisticsDto.setEnterpriseValue(defaultKeyStatistics.path("enterpriseValue").path("raw").asDouble());
            assetStatisticsDto.setForwardPE(defaultKeyStatistics.path("forwardPE").path("raw").asDouble());
            assetStatisticsDto.setProfitMargins(defaultKeyStatistics.path("profitMargins").path("raw").asDouble());
            assetStatisticsDto.setPriceToBook(defaultKeyStatistics.path("priceToBook").path("raw").asDouble());
            assetStatisticsDto.setDebtToEquity(financialData.path("debtToEquity").path("raw").asDouble());
            assetStatisticsDto.setReturnOnEquity(financialData.path("returnOnEquity").path("raw").asDouble());
            assetStatisticsDto.setRevenueDivGrowth(financialData.path("revenueGrowth").path("raw").asDouble());
            assetStatisticsDto.setDividendYield(summaryDetail.path("dividendYield").path("raw").asDouble());
            assetStatisticsDto.setMarketCap(summaryDetail.path("marketCap").path("raw").asDouble());

            return assetStatisticsDto;
        } catch (Exception e) {
            log.error("Error parsing response for symbol: " + symbol, e);
            return null;
        }
    }

    public List<AssetStatisticsDto> getAssetStatistics() {
        List<AssetStatisticsDto> assetStatisticsList = new ArrayList<>();
        for (String symbol : SYMBOLS) {

            AssetStatisticsDto assetStatistics = this.getStockStatistics(symbol);
            if (assetStatistics != null) {
                assetStatisticsList.add(assetStatistics);
            }
        }
        return assetStatisticsList;
    }
    public List<AssetDailyDto> dailyDtos() {
        List<AssetDailyDto> dailyDtoList = new ArrayList<>();
        for (String symbol : SYMBOLS) {
            //log.info("Fetching data for symbol: " + symbol);
            String response = this.getStockData(symbol);
           // log.debug("Data from API for " + symbol + ": " + response);
            AssetDailyDto dailyDto = this.parseResponse(response, symbol);
            if (dailyDto != null) {
                dailyDtoList.add(dailyDto);
            }
        }
        return dailyDtoList;
    }


    @Scheduled(fixedRate = 20000) // Fetch stock data every 60 seconds
    public void sendStockUpdates() {
        List<AssetDailyDto> dailyDto = this.dailyDtos();

        messagingTemplate.convertAndSend("/topic/market", dailyDto);
    }
}
