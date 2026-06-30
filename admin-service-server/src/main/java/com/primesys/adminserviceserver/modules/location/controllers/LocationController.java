package com.primesys.adminserviceserver.modules.location.controllers;

import com.primesys.adminserviceserver.modules.location.dtos.LocationBulkBackupRequestDTO;
import com.primesys.adminserviceserver.modules.location.dtos.LocationHistoryDTO;
import com.primesys.adminserviceserver.modules.location.dtos.LocationTransferRequestDTO;
import com.primesys.adminserviceserver.modules.location.services.LocationService;
import com.primesys.adminserviceserver.modules.location.workflow.LocationWorkflow;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationWorkflow locationWorkflow;

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getLocationHistory(@Valid @RequestParam Long imeiNo,
            @RequestParam Long startTime, @RequestParam Long endTime) {
        List<LocationHistoryDTO> locationHistoryDTOS = locationService.getDeviceLocationHistory(imeiNo, startTime,
                endTime);
        return ResponseHandler.generateResponse(locationHistoryDTOS, true, HttpStatus.OK);
    }

    @PostMapping("/transfer-location")
    public ResponseEntity<Map<String, Object>> copyLocationFromTo(
            @Valid @RequestBody LocationTransferRequestDTO locationTransferRequestDTO) {
        String message = locationWorkflow.transferLocationWithBackup(locationTransferRequestDTO.imeiNos(),
                locationTransferRequestDTO.divisionId(), locationTransferRequestDTO.usedId(),
                locationTransferRequestDTO.fromStartTime(), locationTransferRequestDTO.fromEndTime(),
                locationTransferRequestDTO.toStartTime(), locationTransferRequestDTO.toEndTime());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @PostMapping("/revert-location")
    public ResponseEntity<Map<String, Object>> revertLocationToFrom(
            @Valid @RequestBody LocationTransferRequestDTO locationTransferRequestDTO) {
        String message = locationService.revertLocationCopyProcess(locationTransferRequestDTO.imeiNos(),
                locationTransferRequestDTO.divisionId(), locationTransferRequestDTO.usedId(),
                locationTransferRequestDTO.fromStartTime(), locationTransferRequestDTO.fromEndTime(),
                locationTransferRequestDTO.toStartTime(), locationTransferRequestDTO.toEndTime());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @PostMapping("/destroy-location")
    public ResponseEntity<Map<String, Object>> destroyLocationToFrom(
            @Valid @RequestBody LocationTransferRequestDTO locationTransferRequestDTO) {
        String message = locationService.destroyLocationProcess(locationTransferRequestDTO.imeiNos(),
                locationTransferRequestDTO.divisionId(), locationTransferRequestDTO.usedId(),
                locationTransferRequestDTO.fromStartTime(), locationTransferRequestDTO.fromEndTime(),
                locationTransferRequestDTO.toStartTime(), locationTransferRequestDTO.toEndTime());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }
}
