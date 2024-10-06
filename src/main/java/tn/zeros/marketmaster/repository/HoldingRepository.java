package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Holding;

public interface HoldingRepository extends JpaRepository<Holding,Long> {
}
