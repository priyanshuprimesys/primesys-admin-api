package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicemongodb.entity.OtpEntity;
import com.primesys.adminservicemongodb.repository.OTPRepository;
import com.primesys.adminserviceserver.service.impl.EmailService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@NoArgsConstructor
public class OTPService {

    @Autowired
    private OTPRepository otpRepository;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_TIME_MINUTES = 5;
    @Autowired
    private EmailService emailService;

    // Generate a new OTP
    public String generateOTP(String userId) {
        log.info("generateOTP called for userId='{}'", userId);

        // Check if there's an existing OTP record for this user
        Optional<OtpEntity> existingOtpRecord = otpRepository.findByUserId(userId);

        // If an OTP exists and it's not expired, handle it

        // If an OTP exists, check if it is expired or valid
        if (existingOtpRecord.isPresent()) {
            OtpEntity otpRecord = existingOtpRecord.get();
            log.info("Existing OTP found for userId='{}' expiresAt={} (now={})", userId, otpRecord.getExpiresAt(),
                    LocalDateTime.now());

            // If OTP has expired, delete it
            if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.info("Existing OTP for userId='{}' is expired — deleting and generating a new one", userId);
                otpRepository.delete(otpRecord); // Delete the expired OTP record
            } else {
                // If OTP is still valid, reuse it but re-send the email so the user always receives it
                log.info("Existing OTP for userId='{}' is still valid — reusing it and re-sending the email", userId);
                emailService.sendOTPEmail(userId, otpRecord.getOtp());
                return otpRecord.getOtp();
            }
        }

        // Generate a new OTP and save it
        String otp = generateRandomOTP();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_TIME_MINUTES);

        OtpEntity otpRecord = new OtpEntity();
        otpRecord.setUserId(userId);
        otpRecord.setOtp(otp);
        otpRecord.setExpiresAt(expirationTime);

        otpRepository.save(otpRecord);
        log.info("New OTP generated and saved for userId='{}' otp={} expiresAt={}", userId, otp, expirationTime);

        // Send OTP to email
        log.info("Dispatching OTP email for userId='{}'", userId);
        emailService.sendOTPEmail(userId, otp);

        // Return the generated OTP
        return otp;
    }

    // Verify OTP
    public boolean verifyOTP(String userId, String otp) {
        OtpEntity otpRecord = otpRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("OTP not found for this userId"));

        // Check if OTP is expired
        if (otpRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        // Verify OTP
        return otp.equals(otpRecord.getOtp());
    }

    // Generate random OTP of a specified length
    private String generateRandomOTP() {
        Random rand = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(rand.nextInt(10));
        }
        return otp.toString();
    }

    // @Scheduled(fixedRate = 3600000) // Runs every hour (adjust as needed)
    // public void cleanExpiredOtpRecords() {
    // LocalDateTime now = LocalDateTime.now();
    // otpRepository.deleteByExpiresAtBefore(now);
    // }
}
