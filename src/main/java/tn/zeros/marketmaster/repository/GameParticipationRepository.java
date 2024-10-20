package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.GameParticipation;

public interface GameParticipationRepository extends JpaRepository<GameParticipation,Long> {
}
