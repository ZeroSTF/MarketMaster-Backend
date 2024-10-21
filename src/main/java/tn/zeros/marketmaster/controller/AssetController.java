package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.AssetDTO;
import tn.zeros.marketmaster.dto.PageResponseDTO;
import tn.zeros.marketmaster.service.AssetService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/asset")
@Slf4j
public class AssetController {
    private final AssetService assetService;

    @GetMapping("/getAll")
    public ResponseEntity<PageResponseDTO<AssetDTO>> getAllAssets(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        PageResponseDTO<AssetDTO> response = assetService.getAllAssets(page, size);
        return ResponseEntity.ok(response);
    }
}