package tn.zeros.marketmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.zeros.marketmaster.entity.Transaction;
import tn.zeros.marketmaster.entity.UserWatchlist;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchListDTO {
    private Long id;
    private AssetDTO asset;
    private Long userId;

    public static WatchListDTO fromEntity(UserWatchlist userWatchlist) {
        return WatchListDTO.builder()
                .id(userWatchlist.getId())
                .asset(AssetDTO.fromEntity(userWatchlist.getAsset())) // Map full AssetDTO
                .userId(userWatchlist.getUser().getId())
                .build();
    }
}

