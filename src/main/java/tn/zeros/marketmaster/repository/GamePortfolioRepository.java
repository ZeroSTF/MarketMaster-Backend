package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.GamePortfolio;

public interface GamePortfolioRepository extends JpaRepository<GamePortfolio,Long> {
}
