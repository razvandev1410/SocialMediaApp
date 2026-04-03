package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/getAll")
    @ResponseBody
    public List<User> retrieveAllUsers() {
        return this.userService.retrieveUsers();
    }

    @GetMapping("/getByUserId")
    @ResponseBody
    public User retrieveUserById(@RequestParam("id") Long userId) {
        return this.userService.retrieveUserById(userId);
    }

    @PostMapping("/insertUser")
    @ResponseBody
    public User insertUser(@RequestBody User user) {
        return this.userService.insertUser(user);
    }

    @PutMapping("/updateUser")
    @ResponseBody
    public User updateUser(@RequestBody User user) {
        return this.userService.updateUser(user);
    }

    @DeleteMapping("/deleteById")
    @ResponseBody
    public String deleteUserById(@RequestParam Long userId) {
        return this.userService.deleteById(userId);
    }
}
