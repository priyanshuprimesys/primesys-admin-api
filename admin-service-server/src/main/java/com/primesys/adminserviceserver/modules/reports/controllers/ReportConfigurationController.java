package com.primesys.adminserviceserver.modules.reports.controllers;

import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DeviceReportConfigDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DivisionConfigStatusDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DivisionDeviceReportVariableDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ReportDeviceConfigStatusDTO;
import com.primesys.adminserviceserver.modules.reports.services.ReportConfigurationService;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/report-config")
@RequiredArgsConstructor
public class ReportConfigurationController {

    private final ReportConfigurationService reportConfigurationService;

    @GetMapping("/devices")
    public ResponseEntity<Map<String, Object>> checkDivisionReportEnabled(@Valid @RequestParam String divisionId,
            @RequestParam Integer deviceTypeId) {
        List<DivisionDeviceReportVariableDTO> devices = reportConfigurationService.getDevicesReportConfig(divisionId,
                deviceTypeId);
        return ResponseHandler.generateResponse(devices, true, HttpStatus.OK);
    }

    @PutMapping("/device-report-status")
    public ResponseEntity<Map<String, Object>> updateDeviceReportStatus(
            @Valid @RequestBody ReportDeviceConfigStatusDTO reportDeviceConfigStatusDTO) {
        String message = reportConfigurationService.updateDeviceAndReportStatus(
                reportDeviceConfigStatusDTO.divisionId(), reportDeviceConfigStatusDTO.devices(),
                reportDeviceConfigStatusDTO.deviceTypeId());

        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @PutMapping("/division-device-status")
    public ResponseEntity<Map<String, Object>> updateDivisionDeviceReportStatus(
            @Valid @RequestBody DivisionConfigStatusDTO dto) {
        String message = reportConfigurationService.updateDivisionDeviceAndReportStatus(dto.divisionId(),
                dto.reportEnable(), dto.activeStatus());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @PutMapping("/division-status")
    public ResponseEntity<Map<String, Object>> updateDivisionDeviceReportStatus(@Valid @RequestParam String divisionId,
            @RequestParam Boolean divisionStatus) {
        String message = reportConfigurationService.divisionDeviceAndReportActiveStatus(divisionId, divisionStatus);
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

}
