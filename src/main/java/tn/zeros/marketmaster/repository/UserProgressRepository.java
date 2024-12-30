package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.UserProgress;
import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserId(Long userId);
    List<UserProgress> findByCourseIdAndUserId(Long courseId, Long userId);
    Optional<UserProgress> findByUserIdAndCourseId(Long userId, Long courseId);
}