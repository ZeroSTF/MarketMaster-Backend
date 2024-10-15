package tn.zeros.marketmaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.zeros.marketmaster.entity.Asset;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    public Asset findBySymbol(String symbole);
}