package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.LoginRequestDTO;
import tn.zeros.marketmaster.dto.SignupRequestDTO;
import tn.zeros.marketmaster.dto.UserDTO;
import tn.zeros.marketmaster.dto.TokenResponseDTO;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.CustomAuthenticationException;
import tn.zeros.marketmaster.exception.TokenValidationException;

import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final TokenStorageService tokenStorageService;
    private final UserService userService;
    private final PortfolioService portfolioService;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.access-token.expiration}")
    private long jwtExpiration;
    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public TokenResponseDTO authenticate(LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenService.generateToken(userDetails);
            String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

            tokenStorageService.storeRefreshToken(userDetails.getUsername(), refreshToken);
            String issuedAt = ZonedDateTime.now().toString();

            return new TokenResponseDTO(accessToken, refreshToken, jwtExpiration, issuedAt);
        } catch (BadCredentialsException e) {
            throw new CustomAuthenticationException("Invalid username or password");
        }
    }

    public TokenResponseDTO refreshToken(String refreshToken) {
        String username = jwtTokenService.extractUsername(refreshToken);
        if (!tokenStorageService.validateRefreshToken(username, refreshToken)) {
            throw new TokenValidationException("Invalid refresh token");
        }

        UserDetails userDetails = loadUserByUsername(username);
        String newAccessToken = jwtTokenService.generateToken(userDetails);
        String issuedAt = ZonedDateTime.now().toString();

        return new TokenResponseDTO(newAccessToken, refreshToken, refreshExpiration, issuedAt);
    }

    public void logout(String username) {
        tokenStorageService.removeRefreshToken(username);
        SecurityContextHolder.clearContext();
    }

    public UserDTO signup(SignupRequestDTO signupRequest) {
        User user = userService.signup(signupRequest);
        portfolioService.newPortfolio(user.getUsername());
        return UserDTO.fromEntity(user);
    }

    public String getUsernameFromToken(String token) {
        return jwtTokenService.extractUsername(token.replace("Bearer ", ""));
    }

    public UserDetails loadUserByUsername(String username) {
        return userDetailsService.loadUserByUsername(username);
    }
}