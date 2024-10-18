package tn.zeros.marketmaster.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import tn.zeros.marketmaster.dto.*;
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
    public ResponseEntity<UserDTO> signup(@Valid @RequestBody SignupRequestDTO signupRequest) {
        log.info("Attempting signup for user: {}", signupRequest);
        UserDTO signupResponse = authenticationService.signup(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(signupResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Attempting login for user: {}", loginRequest);
        TokenResponseDTO tokenResponse = authenticationService.authenticate(loginRequest);
        return ResponseEntity.ok(tokenResponse);
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
}