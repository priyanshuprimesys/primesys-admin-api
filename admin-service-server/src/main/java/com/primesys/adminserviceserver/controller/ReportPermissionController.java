package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicecommon.dto.report.ReportPermissionByIdDTO;
import com.primesys.adminservicecommon.dto.report.ReportPermissionDTO;
import com.primesys.adminservicecommon.dto.report.UpdateReportPermissionDTO;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import com.primesys.adminserviceserver.service.ReportPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/report-permission")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReportPermissionController {

    private final ReportPermissionService reportPermissionService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getReportPermission(
            @Valid @ModelAttribute ReportPermissionByIdDTO reportPermissionByIdDTO) {
        ReportPermissionDTO reportPermissionDTOS = reportPermissionService
                .getReportPermissionById(reportPermissionByIdDTO.id());
        return ResponseHandler.generateResponse(reportPermissionDTOS, true, "Report Permission fetched successfully",
                HttpStatus.OK);
    }

    @PatchMapping("/update-permission")
    public ResponseEntity<Map<String, Object>> patchReportPermission(
            @Valid @RequestBody UpdateReportPermissionDTO updateReportPermissionDTO) {
        ReportPermissionDTO reportPermissionDTOS = reportPermissionService.patchModulesList(updateReportPermissionDTO);
        return ResponseHandler.generateResponse(reportPermissionDTOS, true, "Report Permission fetched successfully",
                HttpStatus.OK);
    }
}
