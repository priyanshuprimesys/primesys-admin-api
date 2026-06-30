package com.primesys.adminserviceserver.controller;

import com.primesys.adminserviceserver.dtos.location.DeleteLocationRequestDTO;
import com.primesys.adminserviceserver.dtos.location.DeviceLocationDTO;
import com.primesys.adminserviceserver.dtos.location.DeviceLocationRequestDTO;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import com.primesys.adminserviceserver.service.DeviceLocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/device-locations")
@RequiredArgsConstructor
@Tag(name = "Device Location Controller")
@CrossOrigin("*")
public class DeviceLocationController {

    private final DeviceLocationService deviceLocationService;

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllLocationBetweenTimes(
            @Valid @ModelAttribute DeviceLocationRequestDTO dto) {
        List<DeviceLocationDTO> deviceLocationDTOS = deviceLocationService.getAllLocationBetweenTimestamp(dto);
        return ResponseHandler.generateResponse(deviceLocationDTOS, true, "Locations fetched successfully",
                HttpStatus.OK);
    }

    @DeleteMapping("/destroy")
    public ResponseEntity<Map<String, Object>> deleteAllLocations(
            @Valid @RequestBody DeleteLocationRequestDTO deleteLocationRequestDTO) {
        String message = deviceLocationService.deleteAllLocations(deleteLocationRequestDTO.imei(),
                deleteLocationRequestDTO.timestamp(), deleteLocationRequestDTO.divisionId());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }
}
