package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDTO> retrieveAllUsers() {
        return this.userService.retrieveUsers();
    }

    @GetMapping("/{id}")
    public UserDTO retrieveUserById(@PathVariable("id") Long userId) {
        return this.userService.retrieveUserById(userId);
    }

    @PostMapping("/insertUser")
    public UserDTO insertUser(@RequestBody User user) {
        return this.userService.insertUser(user);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public UserDTO updateUser(@PathVariable("id") Long userId, @RequestBody User user) {
        return this.userService.updateUser(userId, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("id") Long userId) {
        userService.deleteById(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                             .build();
    }

    @PatchMapping("/{id}/ban")
    public UserDTO banUser(@PathVariable Long id, @RequestParam Long moderatorId) {
        return userService.banUser(id, moderatorId);
    }

    @PatchMapping("/{id}/unban")
    public UserDTO unbanUser(@PathVariable Long id, @RequestParam Long moderatorId) {
        return userService.unbanUser(id, moderatorId);
    }
}
