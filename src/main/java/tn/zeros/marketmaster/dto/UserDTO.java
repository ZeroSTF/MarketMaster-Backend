package tn.zeros.marketmaster.dto;

import lombok.Builder;
import lombok.Data;
import tn.zeros.marketmaster.entity.User;

@Data
@Builder
public class UserDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
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