package tn.zeros.marketmaster.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.UserWatchlist;

import java.util.List;

public interface UserWatchlistRepository extends JpaRepository<UserWatchlist, Long> {
    List<UserWatchlist> findByUser_Id(Long userId); // Find watchlist by user ID

    boolean existsByUser_IdAndAsset_Id(Long userId, Long assetId);
    boolean existsByAsset(Asset asset);
    Page<UserWatchlist> findByUser_Username(String username, Pageable pageable);
}
