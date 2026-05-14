package org.example.notificationmicroservice.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twilio")
@Data
public class TwilioConfig {
    private String accountSid;
    private String authToken;
    private String phoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        System.out.println("Twilion succesfully initialized with phone number " + phoneNumber);
    }
}
