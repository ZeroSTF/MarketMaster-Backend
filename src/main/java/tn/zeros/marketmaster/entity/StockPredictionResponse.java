package tn.zeros.marketmaster.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockPredictionResponse {
    private double current_price;
    private String model_path;
    private double predicted_change;
    private double predicted_price;

    // Getters and setters

    @Override
    public String toString() {
        return "StockPredictionResponse{" +
                "current_price=" + current_price +
                ", model_path='" + model_path + '\'' +
                ", predicted_change=" + predicted_change +
                ", predicted_price=" + predicted_price +
                '}';
    }
}
