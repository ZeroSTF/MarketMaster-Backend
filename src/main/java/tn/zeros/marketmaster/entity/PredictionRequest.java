package tn.zeros.marketmaster.entity;

import jakarta.persistence.Entity;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PredictionRequest {
    private String symbol;
    private boolean train;
}
