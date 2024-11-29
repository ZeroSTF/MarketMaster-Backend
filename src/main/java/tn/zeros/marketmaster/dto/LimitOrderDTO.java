package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.LimitOrder;
import tn.zeros.marketmaster.entity.Transaction;
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
    private OrderStatus status;

    public  LimitOrder toEntity() {
        LimitOrder limitOrder = new LimitOrder();
        limitOrder.setLimitPrice(this.limitPrice);
        limitOrder.setQuantity(this.quantity);
        limitOrder.setCreationTimestamp(LocalDateTime.now());
        limitOrder.setStatus(OrderStatus.PENDING);
        limitOrder.setType(type);
        return limitOrder;
    }
    public static LimitOrderDTO fromEntity(LimitOrder limitOrder) {
        return LimitOrderDTO.builder()
                .limitPrice(limitOrder.getLimitPrice())
                .quantity(limitOrder.getQuantity())
                .type(limitOrder.getType())
                .status(limitOrder.getStatus())
                .symbol(limitOrder.getAsset().getSymbol())
                .build();
    }
}
