package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.zeros.marketmaster.entity.Portfolio;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio,Long> {

    Optional<Portfolio> findByUserId(Long userId);

    @Query("SELECT COUNT(p) + 1 FROM Portfolio p WHERE p.annualReturn > (SELECT p2.annualReturn FROM Portfolio p2 WHERE p2.user.id = :userId)")
    Long getRankByUserId(@Param("userId") Long userId);

    @Query("SELECT p.totalValue FROM Portfolio p WHERE p.user.id = :userId AND KEY(p.totalValue) = :date")
    Optional<Double> findTotalValueByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDateTime date);
}
