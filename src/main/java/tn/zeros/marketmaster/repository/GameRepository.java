package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> findByStatus(GameStatus status);

    List<Game> findByStatusAndEndTimestampBefore(GameStatus gameStatus, LocalDateTime now);
}
