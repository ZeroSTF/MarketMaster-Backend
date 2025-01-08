package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.AssetDTO;
import tn.zeros.marketmaster.dto.PageResponseDTO;
import tn.zeros.marketmaster.service.WatchListService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/watchlist")
@Slf4j
public class UserWatchlistController {
    private final WatchListService watchlistService;

    @GetMapping("/{username}")
    public ResponseEntity<PageResponseDTO<AssetDTO>> getUserWatchlist(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        log.info("Fetching watchlist for user: {}", username);
        PageResponseDTO<AssetDTO> response = watchlistService.getUserWatchlist(username, page, size);
        return ResponseEntity.ok(response);
    }
}
