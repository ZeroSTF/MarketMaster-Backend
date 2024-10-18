package tn.zeros.marketmaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.zeros.marketmaster.dto.SignupRequestDTO;
import tn.zeros.marketmaster.dto.UserDTO;
import tn.zeros.marketmaster.entity.User;
import tn.zeros.marketmaster.entity.enums.Role;
import tn.zeros.marketmaster.exception.UserAlreadyExistsException;
import tn.zeros.marketmaster.exception.UserNotFoundException;
import tn.zeros.marketmaster.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User signup(SignupRequestDTO signupRequest) {
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent() || userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
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

        return userRepository.save(user);
    }

    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return UserDTO.fromEntity(user);
    }
}