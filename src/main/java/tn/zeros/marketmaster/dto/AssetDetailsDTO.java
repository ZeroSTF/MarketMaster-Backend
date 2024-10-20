package tn.zeros.marketmaster.dto;

import lombok.Data;

@Data
public class AssetDetailsDTO {
    private String symbol;
    private double enterpriseValue;
    private double forwardPE;
    private double profitMargins;
    private double priceToBook;
    private double debtToEquity;
    private double returnOnEquity;
    private double revenueDivGrowth;
    private double dividendYield;
    private double marketCap;
}
