package org.example.notificationmicroservice.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.example.notificationmicroservice.config.TwilioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    @Autowired
    private TwilioConfig twilioConfig;

    public boolean sendSms(String toPhoneNumber, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    messageBody
            ).create();

            System.out.println("SMS sent succesfully to: " + toPhoneNumber + " | SID: " + message.getSid());

            return true;
        }
        catch(Exception e) {
            System.err.println("Failed to send SMS to " + toPhoneNumber + ": " + e.getMessage());
            return false;
        }
    }
}
