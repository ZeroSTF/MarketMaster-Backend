package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.LimitOrder;

public interface LimitOrderRepository extends JpaRepository<LimitOrder,Long> {
}
