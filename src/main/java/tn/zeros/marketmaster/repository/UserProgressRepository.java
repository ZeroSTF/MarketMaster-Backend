package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.UserProgress;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
}