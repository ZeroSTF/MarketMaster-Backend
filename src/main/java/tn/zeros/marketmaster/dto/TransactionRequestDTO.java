package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.Portfolio;
import tn.zeros.marketmaster.entity.Transaction;
import tn.zeros.marketmaster.entity.enums.TransactionType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    int quantity;
    String symbol;

    //double price;
    TransactionType type;
    public static TransactionRequestDTO fromEntity(Transaction transaction) {
        return TransactionRequestDTO.builder()
                .quantity(transaction.getQuantity()  )
                .type(transaction.getType())
                .build();
    }

    public Transaction toEntity() {
        Transaction transaction = new Transaction();


        transaction.setType(this.type);
        transaction.setQuantity(this.quantity);

        return transaction;
    }
}
