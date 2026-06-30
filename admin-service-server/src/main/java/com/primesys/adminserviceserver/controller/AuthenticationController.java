package com.primesys.adminserviceserver.controller;

import com.primesys.adminserviceserver.request.AuthenticationRequest;
import com.primesys.adminserviceserver.request.RegisterRequest;
import com.primesys.adminserviceserver.response.AuthenticationResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.impl.OTPService;
import com.primesys.adminserviceserver.service.impl.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService service;
    @Autowired
    private OTPService otpService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        service.refreshToken(request, response);
    }

    // Generate OTP
    @PostMapping("/generate-otp")
    public ResponseEntity<HttpApiResponse<String>> generateOTP(@RequestParam String userId) {
        String otp = otpService.generateOTP(userId);
        // return ResponseEntity.ok("OTP generated for userId " + userId + ": " + otp);
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>("OTP generated for userId " + userId,
                Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

    }

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<HttpApiResponse<String>> verifyOTP(@RequestParam String userId, @RequestParam String otp) {
        try {
            boolean isValid = otpService.verifyOTP(userId, otp);
            if (isValid) {
                HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>("OTP is valid for userId " + userId,
                        Boolean.TRUE);
                return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

            } else {
                HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>("OTP is Invalid for userId " + userId,
                        Boolean.FALSE);
                return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

            }
        } catch (Exception e) {
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>("OTP is Invalid for userId " + userId,
                    Boolean.FALSE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.BAD_REQUEST);
        }

    }

}