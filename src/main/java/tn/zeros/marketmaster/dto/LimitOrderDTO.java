package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.LimitOrder;
import tn.zeros.marketmaster.entity.enums.OrderStatus;
import tn.zeros.marketmaster.entity.enums.TransactionType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitOrderDTO {
    private String symbol;
    private int quantity;
    private Double limitPrice;
    private TransactionType type;

    public  LimitOrder toEntity() {
        LimitOrder limitOrder = new LimitOrder();
        limitOrder.setLimitPrice(this.limitPrice);
        limitOrder.setQuantity(this.quantity);
        limitOrder.setCreationTimestamp(LocalDateTime.now());
        limitOrder.setStatus(OrderStatus.PENDING);
        limitOrder.setType(type);
        return limitOrder;
    }
}
