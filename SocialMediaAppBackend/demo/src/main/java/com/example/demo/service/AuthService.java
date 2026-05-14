package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO register(RegisterRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username not available!");
        }

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email not available!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setScore(0.0);
        user.setIsBanned(false);
        user.setIsModerator(false);

        User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    public UserDTO login(LoginRequest request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        User userToLogin = null;

        if(user.isPresent())
            userToLogin = user.get();

        if(userToLogin.getIsBanned())
            throw new IllegalArgumentException("Account banned.");

        if(!passwordEncoder.matches(request.getPassword(), userToLogin.getPassword()))
            throw new IllegalArgumentException("Invalid password!");

        return UserDTO.fromEntity(userToLogin);
    }
}
