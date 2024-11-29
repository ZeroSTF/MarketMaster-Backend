package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BestWinnerDTO {
    private String username;
    private String firstName;
    private String lastName;
    private double price;
}
