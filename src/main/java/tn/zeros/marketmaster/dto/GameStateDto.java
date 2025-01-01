package tn.zeros.marketmaster.dto;

import lombok.Builder;
import lombok.Data;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.GamePortfolio;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GameStateDto {
    private GameMetadataDto gameMetadata;      // Game details
    private GameParticipationDto gameParticipation; // User's participation details
    private GamePortfolioDto gamePortfolio;   // Portfolio (cash, holdings, transactions)
}


