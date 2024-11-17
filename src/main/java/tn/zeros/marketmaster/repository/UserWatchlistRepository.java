package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.UserWatchlist;

import java.util.List;


public interface UserWatchlistRepository extends JpaRepository<UserWatchlist, Long> {
    public List<UserWatchlist> findByUser_Id(Long id);
    boolean existsByAsset(Asset asset);
}