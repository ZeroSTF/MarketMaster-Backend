package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.SignupRequestDTO;
import tn.zeros.marketmaster.dto.UpdatePasswordDTO;
import tn.zeros.marketmaster.dto.UpdateUserDTO;
import tn.zeros.marketmaster.dto.UserDTO;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.enums.Role;
import tn.zeros.marketmaster.exception.InvalidPasswordException;
import tn.zeros.marketmaster.exception.UserAlreadyExistsException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortfolioService portfolioService;

    public User signup(SignupRequestDTO signupRequest) {
        if (userRepository.findByEmailOrUsername(signupRequest.getEmail(), signupRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("User with this email or username already exists");
        }

        User user = User.builder()
                .username(signupRequest.getUsername())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);
        portfolioService.newPortfolio(user.getUsername());
        return user;
    }

    public UserDTO getUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return UserDTO.fromEntity(user);
    }

    public User getUserByUseranme(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    public UserDTO updateUser(String currentUsername, UpdateUserDTO updateUserDTO) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + currentUsername));

        // Check if new username is available (if it's being changed)
        if (!currentUsername.equals(updateUserDTO.getUsername()) &&
                userRepository.findByUsername(updateUserDTO.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        user.setUsername(updateUserDTO.getUsername());
        user.setFirstName(updateUserDTO.getFirstName());
        user.setLastName(updateUserDTO.getLastName());

        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    public void updatePassword(String username, UpdatePasswordDTO updatePasswordDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        // Verify current password
        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }
}
