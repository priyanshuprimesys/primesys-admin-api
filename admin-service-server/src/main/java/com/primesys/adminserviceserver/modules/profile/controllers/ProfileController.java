package com.primesys.adminserviceserver.modules.profile.controllers;

import com.primesys.adminservicecommon.dto.DivisionLoginDto;
import com.primesys.adminserviceserver.modules.profile.services.ProfileService;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import com.primesys.adminserviceserver.service.impl.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtService jwtService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getProfile(@NotNull HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || authHeader.length() < 7) {
            return ResponseHandler.generateResponse("No token passed", false, HttpStatus.FORBIDDEN);
        }
        final String jwt = authHeader.substring(7);
        final String userEmail = jwtService.extractUsername(jwt);
        DivisionLoginDto divisionLoginDto = profileService.getProfile(userEmail);
        return ResponseHandler.generateResponse(divisionLoginDto, true, "Profile fetched successfully", HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestParam String divisionId,
            @RequestBody DivisionLoginDto updateDTO) {
        DivisionLoginDto divisionLoginDto = profileService.updateProfile(divisionId, updateDTO);
        return ResponseHandler.generateResponse(divisionLoginDto, true, "Profile updated successfully", HttpStatus.OK);
    }
}
