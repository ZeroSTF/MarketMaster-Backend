package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.MarketData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MarketDataRepository extends JpaRepository<MarketData , Long> {
    MarketData findTopByGameIdAndAssetIdOrderByTimestampAsc(Long gameId, Long assetId);
    Optional<MarketData> findFirstByGameIdAndAssetIdAndTimestampGreaterThanOrderByTimestampAsc(
            Long gameId, Long assetId, LocalDateTime timestamp);

    @Query("SELECT md FROM MarketData md WHERE md.asset = :asset AND md.game = :game ORDER BY md.timestamp DESC")
    MarketData findTopByAssetAndGameOrderByTimestampDesc(@Param("asset") Asset asset, @Param("game") Game game);



    List<MarketData> findByGameAndAssetAndTimestampBeforeOrderByTimestampAsc(Game game, Asset asset, LocalDateTime timestamp);

    List<MarketData> findTop50ByGameAndAssetAndTimestampAfterOrderByTimestampAsc(Game game, Asset asset, LocalDateTime timestamp);
}
