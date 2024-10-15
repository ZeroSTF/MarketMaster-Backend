package tn.zeros.marketmaster.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import tn.zeros.marketmaster.dto.*;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.exception.CustomAuthenticationException;
import tn.zeros.marketmaster.exception.TokenValidationException;
import tn.zeros.marketmaster.exception.UserAlreadyExistsException;
import tn.zeros.marketmaster.service.AuthenticationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@Valid @RequestBody SignupRequestDTO signupRequest) {
        log.info("Attempting signup for user: {}", signupRequest.getEmail());
        SignupResponseDTO signupResponse = authenticationService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getUsername());
        try {
            TokenResponseDTO tokenResponse = authenticationService.authenticate(loginRequest);
            return ResponseEntity.ok(tokenResponse);
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        log.info("Attempting to refresh token");
        TokenResponseDTO tokenResponse = authenticationService.refreshToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        String username = authenticationService.getUsernameFromToken(token);
        authenticationService.logout(username);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(CustomAuthenticationException e) {
        log.error("Authentication error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(TokenValidationException.class)
    public ResponseEntity<String> handleTokenValidationException(TokenValidationException e) {
        log.error("Token validation error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.error("User registration error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        log.error("Unexpected error: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
}