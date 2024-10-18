package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.repository.AssetRepository;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;

    public double getCurrentPrice(Long assetId) {
        return 100; //TODO BY GADDOUR WITH API
    }
}
