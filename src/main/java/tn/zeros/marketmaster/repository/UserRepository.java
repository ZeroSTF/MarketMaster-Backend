package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}