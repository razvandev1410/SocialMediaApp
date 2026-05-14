package org.example.notificationmicroservice.service;

import org.example.notificationmicroservice.dto.NotificationRequest;
import org.example.notificationmicroservice.entity.Notification;
import org.example.notificationmicroservice.enums.NotificationType;
import org.example.notificationmicroservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    public List<Notification> notifyBannedUser(NotificationRequest request) {
        String reason = null;
        if(request.getReason() != null && !request.getReason().isBlank()) {
            reason = request.getReason();
        }
        else {
            reason = "Inappropiate behavior";
        }

        String emailSubject = "Account banned";
        String emailBody = "Dear " + request.getUsername() + ",\n" + "Your account has been banned due to " + reason + ".\n";
        String smsMessage = "Your account (" + request.getUsername() + ") has been banned on MiniFacebook.";

        boolean emailSent = emailService.sendMail(request.getEmail(), emailSubject, emailBody);

        Notification emailNotification = Notification.builder()
                .recipientMail(request.getEmail())
                .recipientPhone(request.getPhoneNumber())
                .recipientUsername(request.getUsername())
                .subject(emailSubject)
                .message(emailBody)
                .notificationType(NotificationType.EMAIL)
                .isSent(emailSent)
                .build();
        notificationRepository.save(emailNotification);

        boolean smsSent = smsService.sendSms(request.getPhoneNumber(), smsMessage);

        Notification smsNotification = Notification.builder()
                .recipientMail(request.getEmail())
                .recipientPhone(request.getPhoneNumber())
                .recipientUsername(request.getUsername())
                .subject("Account banned")
                .message(smsMessage)
                .notificationType(NotificationType.SMS)
                .isSent(smsSent)
                .build();

        notificationRepository.save(smsNotification);

        return List.of(emailNotification, smsNotification);
    }

    public List<Notification> getNotificationsByUsername(String username) {
        return notificationRepository.findByRecipientUsername(username);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
