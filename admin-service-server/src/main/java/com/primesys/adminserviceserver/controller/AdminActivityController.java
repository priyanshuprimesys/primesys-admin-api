package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicemongodb.entity.AdminActivitySessionEntity;
import com.primesys.adminservicemongodb.entity.AdminDailyActivityEntity;
import com.primesys.adminserviceserver.dtos.activity.CheckinResponse;
import com.primesys.adminserviceserver.request.CheckinRequest;
import com.primesys.adminserviceserver.request.SessionRequest;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.AdminActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/activity")
@CrossOrigin("*")
public class AdminActivityController {

    private final AdminActivityService adminActivityService;

    @PostMapping("/checkin")
    public ResponseEntity<HttpApiResponse<CheckinResponse>> checkin(@RequestBody CheckinRequest request) {
        log.info("checkin 11122 userId={} roleId={}", request.getUserId(), request.getRoleId());
        CheckinResponse response = adminActivityService.checkin(request.getUserId(), request.getUserName(),
                request.getRoleId());
        return new ResponseEntity<>(new HttpApiResponse<>(response, true), HttpStatus.CREATED);
    }

    @PutMapping("/heartbeat")
    public ResponseEntity<HttpApiResponse<String>> heartbeat(@RequestBody SessionRequest request) {
        try {
            return ok(adminActivityService.heartbeat(request.getSessionId(), request.getUserId()));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new HttpApiResponse<>(new ErrorResponse(404, e.getMessage())), HttpStatus.OK);
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<HttpApiResponse<String>> checkout(@RequestBody SessionRequest request) {
        try {
            return ok(adminActivityService.checkout(request.getSessionId(), request.getUserId()));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new HttpApiResponse<>(new ErrorResponse(404, e.getMessage())), HttpStatus.OK);
        }
    }

    @GetMapping("/active-users")
    public ResponseEntity<HttpApiResponse<List<AdminActivitySessionEntity>>> getActiveUsers() {
        log.info("active-users requested");
        return ok(adminActivityService.getActiveSessions());
    }

    @GetMapping("/daily-log")
    public ResponseEntity<HttpApiResponse<List<AdminDailyActivityEntity>>> getDailyLog(@RequestParam String from,
            @RequestParam String to, @RequestParam(required = false) String userId) {
        log.info("daily-log from={} to={} userId={}", from, to, userId);
        return ok(adminActivityService.getDailyLog(from, to, userId));
    }

    private <T> ResponseEntity<HttpApiResponse<T>> ok(T data) {
        return new ResponseEntity<>(new HttpApiResponse<>(data), HttpStatus.OK);
    }
}
