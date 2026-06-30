package com.primesys.adminserviceserver.modules.app_notification.controllers;

import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/app_notification")
public class AppNotificationController {

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> postNotificationToMobile() {
        return ResponseHandler.generateResponse("Succedd call", true, HttpStatus.OK);
    }

}
