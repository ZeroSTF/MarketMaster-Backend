package tn.zeros.marketmaster.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.zeros.marketmaster.service.AssetService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/asset")
public class AssetController {
    private final AssetService assetService;

}
