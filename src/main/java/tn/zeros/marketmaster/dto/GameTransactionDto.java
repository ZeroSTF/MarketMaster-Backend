package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.zeros.marketmaster.entity.GameTransaction;
import tn.zeros.marketmaster.entity.enums.TransactionType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GameTransactionDto {
    private Long id;
    private String symbol;
    private TransactionType type; // BUY or SELL
    private int quantity;
    private double price; // Price per unit
    private LocalDateTime simulationTimestamp;
    private LocalDateTime realTimestamp;

    public GameTransactionDto(GameTransaction transaction) {
        this.id = transaction.getId();
        this.symbol = transaction.getAsset().getSymbol();
        this.type = transaction.getType();
        this.quantity = transaction.getQuantity();
        this.price = transaction.getPrice();
        this.simulationTimestamp = transaction.getSimulationTimestamp();
        this.realTimestamp = transaction.getRealTimestamp();
    }
}
