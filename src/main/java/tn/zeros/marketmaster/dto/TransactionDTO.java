package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.Transaction;
import tn.zeros.marketmaster.entity.enums.TransactionType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    Long id;
    String symbol;
    int quantity;
    double price;
    LocalDateTime timeStamp;
    TransactionType type;
    @Override
    public String toString() {
        return "TransactionDTO{" +
                "symbol='" + symbol + '\'' +
                ", quantity=" + quantity +
                ", type=" + type +
                ", price=" + price +
                ", timeStamp=" + timeStamp +
                '}';
    }
    public Transaction toEntity() {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setType(this.type);
        transaction.setQuantity(this.quantity);
        transaction.setPrice(this.price);
        transaction.setTimestamp(this.timeStamp);
        return transaction;
    }

    public static TransactionDTO fromEntity(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .price(transaction.getPrice())
                .quantity(transaction.getQuantity())
                .symbol(transaction.getAsset().getSymbol())
                .timeStamp(transaction.getTimestamp())
                .build();
    }

}
