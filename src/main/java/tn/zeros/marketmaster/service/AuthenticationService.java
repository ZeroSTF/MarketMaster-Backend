package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
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
import tn.zeros.marketmaster.dto.SignupResponseDTO;
import tn.zeros.marketmaster.dto.TokenResponseDTO;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.CustomAuthenticationException;
import tn.zeros.marketmaster.exception.TokenValidationException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final TokenStorageService tokenStorageService;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public TokenResponseDTO authenticate(LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtTokenService.generateToken(userDetails);
            String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

            tokenStorageService.storeRefreshToken(userDetails.getUsername(), refreshToken);

            return new TokenResponseDTO(accessToken, refreshToken);
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

        return new TokenResponseDTO(newAccessToken, refreshToken);
    }

    public void logout(String username) {
        tokenStorageService.removeRefreshToken(username);
        SecurityContextHolder.clearContext();
    }

    public SignupResponseDTO signup(SignupRequestDTO signupRequest) {
        User user = userService.signup(signupRequest);
        return SignupResponseDTO.fromEntity(user);
    }

    public String getUsernameFromToken(String token) {
        return jwtTokenService.extractUsername(token.replace("Bearer ", ""));
    }

    public UserDetails loadUserByUsername(String username) {
        return userDetailsService.loadUserByUsername(username);
    }
}