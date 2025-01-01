package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.LimitOrder;
import tn.zeros.marketmaster.entity.User;

import java.util.List;

public interface LimitOrderRepository extends JpaRepository<LimitOrder,Long> {
    List<LimitOrder> findByUserId(Long userId);
    @Modifying
    @Query("DELETE FROM LimitOrder lo WHERE lo.asset = :asset AND lo.limitPrice = :limitPrice AND lo.user = :user")
    void deleteLimitOrderByAssetAndLimitPriceAndUser(Asset asset, Double limitPrice, User user);
}
