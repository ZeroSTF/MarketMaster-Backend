package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Game;

public interface GameRepository extends JpaRepository<Game, Long> {

}
