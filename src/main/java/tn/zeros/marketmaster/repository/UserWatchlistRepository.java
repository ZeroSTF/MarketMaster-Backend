package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.UserWatchlist;

public interface UserWatchlistRepository extends JpaRepository<UserWatchlist, Long> {
}