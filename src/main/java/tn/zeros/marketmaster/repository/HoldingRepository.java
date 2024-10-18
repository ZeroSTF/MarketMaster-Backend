package tn.zeros.marketmaster.repository;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.Holding;
import tn.zeros.marketmaster.entity.Portfolio;

import java.util.Set;

public interface HoldingRepository extends JpaRepository<Holding,Long> {

    @Query("SELECT count(*) from Holding h where h.portfolio.id=:user")
    public int countHoldingByUserId(@Param("user") Long id);

    public Set<Holding> findAllByPortfolio(Portfolio p);
}
