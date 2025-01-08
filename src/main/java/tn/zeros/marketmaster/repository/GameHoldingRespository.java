package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.GameHolding;

import java.util.List;

public interface GameHoldingRespository extends JpaRepository<GameHolding , Long> {
}
