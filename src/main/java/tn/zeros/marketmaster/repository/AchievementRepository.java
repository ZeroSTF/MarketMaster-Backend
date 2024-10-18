package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Achievement;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
}