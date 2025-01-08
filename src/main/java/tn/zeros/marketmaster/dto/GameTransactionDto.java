package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.zeros.marketmaster.entity.GameTransaction;
import tn.zeros.marketmaster.entity.enums.TransactionType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GameTransactionDto {
    private Long gameId;
    private String symbol;
    private TransactionType type;
    private int quantity;
    private LocalDateTime simulationTimestamp;
    private String username; // New field to identify the user

    public GameTransactionDto(GameTransaction transaction) {
        this.gameId = transaction.getId();
        this.symbol = transaction.getAsset().getSymbol();
        this.type = transaction.getType();
        this.quantity = transaction.getQuantity();
        this.simulationTimestamp = transaction.getSimulationTimestamp();

    }
}
