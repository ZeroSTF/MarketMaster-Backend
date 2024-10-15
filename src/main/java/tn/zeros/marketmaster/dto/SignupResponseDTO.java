package tn.zeros.marketmaster.dto;

import lombok.Builder;
import lombok.Data;
import tn.zeros.marketmaster.entity.User;

@Data
@Builder
public class SignupResponseDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public static SignupResponseDTO fromEntity(User user) {
        return SignupResponseDTO.builder()
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    public User toEntity(){
        User user = new User();
        user.setUsername(this.username);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        return user;
    }
}