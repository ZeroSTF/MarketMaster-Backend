package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.zeros.marketmaster.entity.Asset;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Asset findBySymbol(String symbol);

}