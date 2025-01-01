package tn.zeros.marketmaster.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateTimestampRequest {
    private Long gameParticipationId;
    private LocalDateTime lastPauseTimestamp;
}
