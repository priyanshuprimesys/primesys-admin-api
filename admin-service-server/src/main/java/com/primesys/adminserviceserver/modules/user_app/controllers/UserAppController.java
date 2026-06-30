package com.primesys.adminserviceserver.modules.user_app.controllers;

import com.primesys.adminserviceserver.modules.user_app.dtos.UserAppInstallRequestDTO;
import com.primesys.adminserviceserver.modules.user_app.services.UserAppService;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/user_app")
@RequiredArgsConstructor
public class UserAppController {

    private final UserAppService userAppService;

    @PostMapping("/install")
    public ResponseEntity<Map<String, Object>> install(@RequestBody UserAppInstallRequestDTO dto) {
        userAppService.createUserApp(dto);
        return ResponseHandler.generateResponse("App registered successfully", true, HttpStatus.OK);
    }
}
