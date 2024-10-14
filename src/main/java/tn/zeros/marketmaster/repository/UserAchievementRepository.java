package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.UserAchievement;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
}