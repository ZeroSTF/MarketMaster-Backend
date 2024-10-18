package tn.zeros.marketmaster.repository;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.entity.Holding;
import tn.zeros.marketmaster.entity.Portfolio;

public interface HoldingRepository extends JpaRepository<Holding,Long> {
    public Holding findByAssetAndPortfolio(Asset a, Portfolio p);
}
