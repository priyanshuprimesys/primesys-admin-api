package com.primesys.adminserviceserver.modules.jobs.controllers;

import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/email")
public class EmailController {

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllReportEmail() {
        String message = "all emails";
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }
}
