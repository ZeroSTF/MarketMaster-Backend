package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.LimitOrder;

import java.util.List;

public interface LimitOrderRepository extends JpaRepository<LimitOrder,Long> {
    List<LimitOrder> findByUserId(Long userId);
}
