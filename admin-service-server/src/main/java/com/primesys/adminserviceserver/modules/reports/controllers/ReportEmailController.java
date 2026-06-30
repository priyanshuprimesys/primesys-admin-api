package com.primesys.adminserviceserver.modules.reports.controllers;

import com.primesys.adminserviceserver.modules.reports.dtos.AdminQueueViewDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailQueueLogDTO;
import com.primesys.adminserviceserver.modules.reports.services.ReportEmailService;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/report-email")
@RequiredArgsConstructor
@Tag(name = "Report Email Controller")
public class ReportEmailController {

    private final ReportEmailService reportEmailService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAllReportEmailStatusLogs(@Valid @RequestParam Long reportDate) {
        List<ReportEmailDTO> reportEmailDTOS = reportEmailService.getAllReportEmailStatus(reportDate);
        return ResponseHandler.generateResponse(reportEmailDTOS, true, "Report Email Statuses fetched", HttpStatus.OK);
    }

    @GetMapping("/system-view")
    public ResponseEntity<Map<String, Object>> getAdminSystemView(@RequestParam(required = false) Long reportDate,
            @RequestParam(defaultValue = "false") boolean includeLogs) {
        List<AdminQueueViewDTO> view = reportEmailService.getAdminSystemView(reportDate, includeLogs);
        return ResponseHandler.generateResponse(view, "System view fetched", HttpStatus.OK);
    }

    @GetMapping("/process-logs")
    public ResponseEntity<Map<String, Object>> getProcessLogsByQueueId(@RequestParam String queueId) {
        List<ReportEmailQueueLogDTO> logs = reportEmailService.getProcessLogsByQueueId(queueId);
        return ResponseHandler.generateResponse(logs, "Process logs fetched", HttpStatus.OK);
    }

    @GetMapping("/process-logs/active")
    public ResponseEntity<Map<String, Object>> getActiveProcessLogs() {
        List<ReportEmailQueueLogDTO> logs = reportEmailService.getActiveProcessLogs();
        return ResponseHandler.generateResponse(logs, "Active process logs fetched", HttpStatus.OK);
    }

    @GetMapping("/process-logs/by-division")
    public ResponseEntity<Map<String, Object>> getProcessLogsByDivisionAndDate(@RequestParam String divisionId,
            @RequestParam Integer deviceTypeId, @RequestParam Long reportDate) {
        List<ReportEmailQueueLogDTO> logs = reportEmailService.getProcessLogsByDivisionAndDate(divisionId, deviceTypeId,
                reportDate);
        return ResponseHandler.generateResponse(logs, "Process logs fetched", HttpStatus.OK);
    }

}
