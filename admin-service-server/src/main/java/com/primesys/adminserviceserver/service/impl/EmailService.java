package com.primesys.adminserviceserver.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    // Spring-managed mail sender, autoconfigured from spring.mail.* in application.properties. Using this
    // (jakarta.mail)
    // instead of a hand-rolled javax.mail Session avoids the activation-1.1 / Java 17 IllegalAccessError.
    private final JavaMailSender mailSender;

    public void sendOTPEmail(String userId, String otp) {
        // Recipient is hardcoded (company inboxes), not derived from userId.
        String receiverEmail = "contact@primesystech.com,patil.rupesh4892@gmail.com";
        log.info("sendOTPEmail userId='{}' -> recipients='{}' (recipient is hardcoded, not derived from userId)",
                userId, receiverEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("primesystechreport56@gmail.com");
            message.setTo(receiverEmail.split(","));
            message.setSubject("OTP for Report generation");
            message.setText(
                    " Generated Report OTP for userId " + userId + " :  " + otp + " which will valid for 5 min.");

            log.info("Sending OTP email to '{}' ...", receiverEmail);
            mailSender.send(message);
            log.info("OTP email sent successfully to '{}' for userId='{}'", receiverEmail, userId);

        } catch (Exception e) {
            log.error("Failed to send OTP email to '{}' for userId='{}': {}", receiverEmail, userId, e.getMessage(), e);
        }
    }

}
