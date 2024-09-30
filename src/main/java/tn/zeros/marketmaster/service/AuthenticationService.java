package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.LoginRequestDTO;
import tn.zeros.marketmaster.dto.TokenResponseDTO;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.TokenValidationException;
import tn.zeros.marketmaster.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final TokenStorageService tokenStorageService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public TokenResponseDTO authenticate(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtTokenService.generateToken(userDetails);
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

        tokenStorageService.storeRefreshToken(userDetails.getUsername(), refreshToken);

        return new TokenResponseDTO(accessToken, refreshToken);
    }

    public TokenResponseDTO refreshToken(String refreshToken) {
        String username = jwtTokenService.extractUsername(refreshToken);
        if (!tokenStorageService.validateRefreshToken(username, refreshToken)) {
            throw new TokenValidationException("Invalid refresh token");
        }

        UserDetails userDetails = loadUserByUsername(username);
        String newAccessToken = jwtTokenService.generateToken(userDetails);

        return new TokenResponseDTO(newAccessToken, refreshToken);
    }

    public void logout(String username) {
        tokenStorageService.removeRefreshToken(username);
        SecurityContextHolder.clearContext();
    }
}