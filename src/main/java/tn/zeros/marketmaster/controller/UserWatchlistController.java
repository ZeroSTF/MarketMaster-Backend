package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.PageResponseDTO;
import tn.zeros.marketmaster.dto.WatchListDTO;
import tn.zeros.marketmaster.entity.Asset;
import tn.zeros.marketmaster.service.UserService;
import tn.zeros.marketmaster.service.WatchListService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/watchlist")
@Slf4j
public class UserWatchlistController {
    private final WatchListService watchlistService;
    private final UserService userService;


    @GetMapping("/{username}")
    public ResponseEntity<PageResponseDTO<WatchListDTO>> getUserWatchlist(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("Fetching watchlist for user: {}", username);
        PageResponseDTO<WatchListDTO> response = watchlistService.getWatchlistByUser(username, page, size);
        return ResponseEntity.ok(response);
    }
}
