package com.primesys.adminserviceserver.modules.auth.controllers;

import com.primesys.adminserviceserver.modules.auth.dtos.AuthRequest;
import com.primesys.adminserviceserver.modules.auth.dtos.AuthResponse;
import com.primesys.adminserviceserver.modules.auth.services.AuthService;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v2/authenticate")
@RequiredArgsConstructor
@Tag(name = "New Auth Controller")
public class AuthController {

    private final AuthService authService;

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.authenticate(authRequest.email(), authRequest.password());
        return ResponseHandler.generateResponse(response, true, "Logged in successfully", HttpStatus.OK);
    }
}
