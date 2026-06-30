package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicemongodb.entity.PendingWhitelistEntity;
import com.primesys.adminserviceserver.request.WhitelistRequest;
import com.primesys.adminserviceserver.request.WhitelistStatusUpdateRequest;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.WhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/v2/whitelist")
@RequiredArgsConstructor
public class WhitelistController {

    private final WhitelistService whitelistService;

    @GetMapping
    public ResponseEntity<HttpApiResponse<List<PendingWhitelistEntity>>> getWhitelist(
            @RequestParam(required = false) String status) {
        log.info("get whitelist status {}", status);
        List<PendingWhitelistEntity> whitelist = whitelistService.getWhitelist(status);
        return new ResponseEntity<>(new HttpApiResponse<>(whitelist), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<HttpApiResponse<List<PendingWhitelistEntity>>> createWhitelist(
            @RequestBody WhitelistRequest request) {
        log.info("create whitelist request {}", request);
        List<PendingWhitelistEntity> created = whitelistService.createWhitelist(request);
        return new ResponseEntity<>(new HttpApiResponse<>(created, Boolean.TRUE), HttpStatus.OK);
    }

    /**
     * Update a single whitelist entry's status (one FN row or one SOS row), keyed by its own id.
     *
     * PATCH /v2/whitelist/{id}/status body: { "status": "COMPLETED", "updatedBy": "admin" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<HttpApiResponse<PendingWhitelistEntity>> updateStatus(@PathVariable String id,
            @RequestBody WhitelistStatusUpdateRequest request) {
        log.info("update whitelist status id={} status={}", id, request.getStatus());
        PendingWhitelistEntity updated = whitelistService.updateStatus(id, request.getStatus(), request.getUpdatedBy());
        return new ResponseEntity<>(new HttpApiResponse<>(updated, Boolean.TRUE), HttpStatus.OK);
    }

    /**
     * Update the status of every whitelist entry for a device (both FN and SOS) in one call, keyed by device imei.
     *
     * PATCH /v2/whitelist/device/{deviceImei}/status body: { "status": "COMPLETED", "updatedBy": "admin" }
     */
    @PatchMapping("/device/{deviceImei}/status")
    public ResponseEntity<HttpApiResponse<List<PendingWhitelistEntity>>> updateStatusByDevice(
            @PathVariable Long deviceImei, @RequestBody WhitelistStatusUpdateRequest request) {
        log.info("update whitelist status by imei={} status={}", deviceImei, request.getStatus());
        List<PendingWhitelistEntity> updated = whitelistService.updateStatusByDeviceImei(deviceImei,
                request.getStatus(), request.getUpdatedBy());
        return new ResponseEntity<>(new HttpApiResponse<>(updated, Boolean.TRUE), HttpStatus.OK);
    }
}
