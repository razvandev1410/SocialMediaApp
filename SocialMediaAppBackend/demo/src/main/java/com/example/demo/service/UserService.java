package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.*;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RestTemplate restTemplate;

    public List<UserDTO> retrieveUsers() {
        return userRepository.findAll().stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDTO retrieveUserById(Long userId) {
        Optional<User> user = this.userRepository.findById(userId);
        if(user.isPresent())
            return UserDTO.fromEntity(user.get());
        else
            return null;
    }

    public User findEntityById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent())
            return user.get();
        else
            throw new ResourceNotFoundException("User with id " + userId + " not found!");
    }

    public UserDTO insertUser(User user) {
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if(user.getScore() == null)
            user.setScore(0.0);

        user.setIsBanned(false);
        user.setIsModerator(false);

        User saved = userRepository.save(user);
        return UserDTO.fromEntity(saved);
    }

    public UserDTO updateUser(Long userId, User updatedUser) {
        Optional<User> existingUser = userRepository.findById(userId);
        User userToUpdate = null;
        if(existingUser.isPresent())
            userToUpdate = existingUser.get();
        else
            throw new ResourceNotFoundException("User with id " + userId + " doesn't exist!");

        if(updatedUser.getUsername() != null) {
            userToUpdate.setUsername(updatedUser.getUsername());
        }

        if(updatedUser.getEmail() != null) {
            userToUpdate.setEmail(updatedUser.getEmail());
        }

        if(updatedUser.getPhoneNumber() != null) {
            userToUpdate.setPhoneNumber(updatedUser.getPhoneNumber());
        }

        if(updatedUser.getPassword() != null && updatedUser.getPassword().isBlank() == false) {
            userToUpdate.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return UserDTO.fromEntity(userToUpdate);
    }

    public String deleteById(Long userId) {
        if(!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User with id " + userId + " doesn't exist!");
        }
        try {
            this.userRepository.deleteById(userId);
        }
        catch (Exception e) {
            return "Failed deleting user " + userId;
        }
        return "User deletion successful";
    }

    public void updateScore(Long userId, double points) {
        User user = findEntityById(userId);
        user.setScore(user.getScore() + points);
        userRepository.save(user);
    }

    public UserDTO banUser(Long userId, Long moderatorId) {
        User moderator = findEntityById(moderatorId);
        if(!moderator.getIsModerator()) {
            throw new IllegalArgumentException("Only moderators can ban users!");
        }

        User user = findEntityById(userId);
        user.setIsBanned(true);
        userRepository.save(user);

        try {
            Map<String, String> notificationRequest = new HashMap<>();
            notificationRequest.put("username", user.getUsername());
            notificationRequest.put("email", user.getEmail());
            notificationRequest.put("phoneNumber", user.getPhoneNumber());
            notificationRequest.put("reason", "Banned");

            restTemplate.postForObject(
                "http://notification-service:8082/notifications/ban",
                    notificationRequest,
                    Object.class
            );
        }
        catch(Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }

        return UserDTO.fromEntity(user);
    }

    public UserDTO unbanUser(Long userId, Long moderatorId) {
        User moderator = findEntityById(moderatorId);
        if(!moderator.getIsModerator()) {
            throw new IllegalArgumentException("Only moderators can unban users!");
        }
        User user = findEntityById(userId);
        user.setIsBanned(false);
        return UserDTO.fromEntity(userRepository.save(user));
    }
}
