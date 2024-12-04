package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.MarketData;

import java.util.List;
import java.util.Map;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetHistoryResponse {
    private Map<String, List<MarketData>> data;  // Map of symbol to list of MarketData


}