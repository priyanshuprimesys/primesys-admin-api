package com.primesys.adminserviceserver.modules.reports.controllers;

import com.primesys.adminserviceserver.modules.reports.dtos.*;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DivisionReportConfigDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ModuleMasterDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ReportConfigDTO;
import com.primesys.adminserviceserver.modules.reports.services.ReportService;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/report")
@RequiredArgsConstructor
@Tag(name = "Report Controller")
@Slf4j
@CrossOrigin("*")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getReportConfig(@Valid @RequestParam String divisionId) {
        ReportConfigDTO reportConfigDTOS = reportService.getConfig(divisionId);
        return ResponseHandler.generateResponse(reportConfigDTOS, true, "Report config fetched successfully",
                HttpStatus.OK);
    }

    @GetMapping("/config-detail")
    public ResponseEntity<Map<String, Object>> getReportConfigDetail(@Valid @RequestParam String divisionId) {
        DivisionReportConfigDTO reportConfigDTO = reportService.getDivisionReportDetail(divisionId);
        return ResponseHandler.generateResponse(reportConfigDTO, true, "Report Config detail fetched successfully",
                HttpStatus.OK);
    }

    @GetMapping("/report-module-list")
    public ResponseEntity<Map<String, Object>> getReportModuleList(@Valid @RequestParam String divisionId) {
        List<ModuleMasterDTO> moduleMasterDTOS = reportService.getReportModuleList(divisionId);
        return ResponseHandler.generateResponse(moduleMasterDTOS, true, "Module List fetched successfully",
                HttpStatus.OK);
    }

    @GetMapping("/log")
    public ResponseEntity<Map<String, Object>> getDivisionReportLog(@Valid @RequestParam String divisionId,
            Integer deviceTypeId, Long reportDate) {
        List<DivisionReportLogDTO> divisionTripReports = reportService.getDivisionReportLog(divisionId, deviceTypeId,
                reportDate);
        return ResponseHandler.generateResponse(divisionTripReports, true, "Report fetched successfully",
                HttpStatus.OK);
    }

    @GetMapping("/trip-report-summary")
    public ResponseEntity<Map<String, Object>> getTripReportSummary(@Valid @RequestParam String divisionId,
            @RequestParam Integer deviceType, @RequestParam Long startDateTime, @RequestParam Long endDateTime) {
        List<TripStatusSummaryDTO> reportDTOS = reportService.getTripReportStatusSummary(divisionId, deviceType,
                startDateTime, endDateTime);
        return ResponseHandler.generateResponse(reportDTOS, true, "Reports fetched successfully", HttpStatus.OK);
    }

    @PostMapping("/regenerate-trip-summary")
    public ResponseEntity<Map<String, Object>> getTripSummaryRegenerate(
            @Valid @RequestBody TripSummaryRegenerateRequestDTO dto) {
        String message = reportService.getTripSummaryRegeneratedReport(dto.divisionId(), dto.deviceType(),
                dto.reportDate());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @PostMapping("/regenerate-report-log")
    public ResponseEntity<Map<String, Object>> fetchRegenerateDivisionReport(
            @Valid @RequestBody DivisionReportLogRequestDTO divisionReportLogRequestDTO) {
        String message = reportService.getRegeneratedReportLog(divisionReportLogRequestDTO.divisionId(),
                divisionReportLogRequestDTO.deviceTypeId(), divisionReportLogRequestDTO.reportDate());
        return ResponseHandler.generateResponse(message, true, "Report regeneration called successfully",
                HttpStatus.OK);
    }

    @PutMapping("/report-log-status")
    public ResponseEntity<Map<String, Object>> updateReportLogStatus(@Valid @RequestParam String reportId,
            @RequestParam String divisionId, @RequestParam Integer deviceTypeId, @RequestParam Long reportDate,
            @RequestParam String status) {
        String message = reportService.updateReportLogStatus(reportId, divisionId, deviceTypeId, reportDate, status);
        return ResponseHandler.generateResponse(message, true, "Report Activated", HttpStatus.OK);
    }

    @PutMapping("/update-module-list")
    public ResponseEntity<Map<String, Object>> getUpdateReportModuleList(
            @Valid @RequestBody DivisionReportModuleUpdateDTO dto) {
        String message = reportService.updateModuleList(dto.divisionId(), dto.moduleList());
        return ResponseHandler.generateResponse(message, true, "MODULES Updated", HttpStatus.OK);
    }

    @DeleteMapping("/destroy-log")
    public ResponseEntity<Map<String, Object>> destroyReportLog(@Valid @RequestBody DestroyReportLogRequestDTO dto) {
        String message = reportService.destroyReportLog(dto.reportId());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @DeleteMapping("/destroy-all-log")
    public ResponseEntity<Map<String, Object>> destroyReportLog(@Valid @RequestBody DestroyAllReportLogDTO dto) {
        String message = reportService.destroyReportLog(dto.divisionId());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @DeleteMapping("/destroy-trip-summary-report")
    public ResponseEntity<Map<String, Object>> destroyTripSummaryReport(
            @Valid @RequestBody DestroyTripSummaryReportDTO dto) {
        log.info("request {}", dto);
        String message = reportService.destroyTripReportSummary(dto.divisionId(), dto.deviceType(), dto.reportDate());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @GetMapping("/email-log")
    public ResponseEntity<Map<String, Object>> getReportEmailLog(@Valid @RequestParam String divisionId,
            @RequestParam Integer deviceTypeId, @RequestParam Long reportDate) {
        List<ReportEmailResponseDTO> reportEmailResponseDTOS = reportService.getEmailReportLogs(divisionId,
                deviceTypeId, reportDate);
        return ResponseHandler.generateResponse(reportEmailResponseDTOS, "Logs fetched", HttpStatus.OK);
    }

    @PostMapping("/scheduleEmail")
    public ResponseEntity<Map<String, Object>> getReportEmailLog(
            @Valid @RequestBody ReportEmailRequestDTO emailRequestDTO) {
        String message = reportService.scheduleReportEmail(emailRequestDTO.divisionId(), emailRequestDTO.deviceTypeId(),
                emailRequestDTO.reportDate(), emailRequestDTO.userId());
        return ResponseHandler.generateResponse(message, "Logs fetched", HttpStatus.OK);
    }

}