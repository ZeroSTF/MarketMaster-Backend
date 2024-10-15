package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}