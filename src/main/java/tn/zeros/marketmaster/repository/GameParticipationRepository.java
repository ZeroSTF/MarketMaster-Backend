package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Game;
import tn.zeros.marketmaster.entity.GameParticipation;
import tn.zeros.marketmaster.entity.User;

public interface GameParticipationRepository extends JpaRepository<GameParticipation,Long> {
    boolean existsByGameAndUser(Game game, User user);
}
