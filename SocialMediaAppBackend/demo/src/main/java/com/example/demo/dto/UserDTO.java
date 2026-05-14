package com.example.demo.dto;

import com.example.demo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private Double score;
    private Boolean isBanned;
    private Boolean isModerator;

    public static UserDTO fromEntity(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getScore(),
                user.getIsBanned(),
                user.getIsModerator()
        );
    }
}
