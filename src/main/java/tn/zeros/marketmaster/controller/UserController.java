package tn.zeros.marketmaster.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import tn.zeros.marketmaster.dto.UpdatePasswordDTO;
import tn.zeros.marketmaster.dto.UpdateUserDTO;
import tn.zeros.marketmaster.dto.UserDTO;
import tn.zeros.marketmaster.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        log.info("Fetching current user data for: {}", username);
        UserDTO userDTO = userService.getUser(username);
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/update")
    public ResponseEntity<UserDTO> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateUserDTO updateUserDTO) {
        log.info("Updating user data for: {}", userDetails.getUsername());
        UserDTO updatedUser = userService.updateUser(userDetails.getUsername(), updateUserDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/update-password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        log.info("Updating password for user: {}", userDetails.getUsername());
        userService.updatePassword(userDetails.getUsername(), updatePasswordDTO);
        return ResponseEntity.ok().build();
    }
}
