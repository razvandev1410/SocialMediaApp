package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> retrieveUsers() {
        return (List<User>) this.userRepository.findAll();
    }

    public User retrieveUserById(Long userId) {
        Optional<User> user = this.userRepository.findById(userId);
        if(user.isPresent())
            return user.get();
        else
            return null;
    }

    public User insertUser(User user) {
        if(user.getScore() == null)
            user.setScore(0.0);
        return this.userRepository.save(user);
    }

    public User updateUser(User user) {
        return this.userRepository.save(user);
    }

    public String deleteById(Long userId) {
        if(!userRepository.existsById(userId)) {
            return "User doesn't exist";
        }
        try {
            this.userRepository.deleteById(userId);
        }
        catch (Exception e) {
            return "Failed deleting user " + userId;
        }
        return "User deletion successful";
    }
}
