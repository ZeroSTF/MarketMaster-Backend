package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private String issuedAt;
}