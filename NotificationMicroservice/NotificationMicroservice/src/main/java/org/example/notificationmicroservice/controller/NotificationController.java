package org.example.notificationmicroservice.controller;

import jakarta.validation.Valid;
import org.example.notificationmicroservice.dto.NotificationRequest;
import org.example.notificationmicroservice.entity.Notification;
import org.example.notificationmicroservice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/ban")
    public ResponseEntity<List<Notification>> notifyBannedUser(@Valid @RequestBody NotificationRequest request) {
        List<Notification> notifications = notificationService.notifyBannedUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
    }

    @GetMapping("/user/{username}")
    public List<Notification> getByUsername(@PathVariable String username) {
        return notificationService.getNotificationsByUsername(username);
    }

    @GetMapping
    public List<Notification> getAll() {
        return notificationService.getAllNotifications();
    }
}
