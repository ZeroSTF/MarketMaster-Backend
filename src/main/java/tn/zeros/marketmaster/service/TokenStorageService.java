package tn.zeros.marketmaster.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenStorageService {
    private final Map<String, String> refreshTokenStore = new HashMap<>();

    public void storeRefreshToken(String username, String refreshToken) {
        refreshTokenStore.put(username, refreshToken);
    }

    public boolean validateRefreshToken(String username, String refreshToken) {
        String storedToken = refreshTokenStore.get(username);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void removeRefreshToken(String username) {
        refreshTokenStore.remove(username);
    }
}