package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username can't be blank / null")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Password can't be blank / null")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Email can't be blank / null")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number can't be blank / null")
    private String phoneNumber;
}