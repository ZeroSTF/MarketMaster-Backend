package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.enums.TransactionType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
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
}
