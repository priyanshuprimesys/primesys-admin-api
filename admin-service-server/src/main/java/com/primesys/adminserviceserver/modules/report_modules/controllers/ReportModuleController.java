package com.primesys.adminserviceserver.modules.report_modules.controllers;

import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;
import com.primesys.adminservicemongodb.model.UserReportModuleModel;
import com.primesys.adminserviceserver.modules.report_modules.dtos.ModuleMasterCreateDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.ReportModuleCreateDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.ReportModuleResponseDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.ReportModuleUpdateDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport.DivisionCreateReportModule;
import com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport.DivisionReportHierarchyResponseDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport.DivisionReportModuleResponseDTO;
import com.primesys.adminserviceserver.modules.report_modules.services.ReportModuleService;
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
@RequestMapping("/api/v2/report-modules")
@RequiredArgsConstructor
@Tag(name = "Report Module Controller")
@Slf4j
public class ReportModuleController {
    private final ReportModuleService reportModuleService;

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getUserModules() {
        List<ReportModuleResponseDTO> reportModuleDTOS = reportModuleService.getUserReportModules();
        return ResponseHandler.generateResponse(reportModuleDTOS, true, "Modules Fetched successfully", HttpStatus.OK);
    }

    // @GetMapping("/all")
    // public ResponseEntity<Map<String,Object>> getAllModules(){
    // List<ReportModuleResponseDTO> reportModuleResponseDTOS = reportModuleService.getAllReportModule();
    // return ResponseHandler.generateResponse(reportModuleResponseDTOS,true,"Modules Fetched
    // successfully",HttpStatus.OK);
    // }

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getReportModuleById(@Valid @RequestParam String moduleId) {
        ReportModuleResponseDTO responseDTO = reportModuleService.getModuleById(moduleId);
        return ResponseHandler.generateResponse(responseDTO, true, "Module fetched successfully", HttpStatus.OK);
    }

    @GetMapping("/hierarchy-report-module")
    public ResponseEntity<Map<String, Object>> getDivisionHierarchyReportModule(
            @Valid @RequestParam String divisionId) {
        DivisionReportHierarchyResponseDTO divisionReportModuleResponseDTO = reportModuleService
                .getDivisionHierarchyReportModules(divisionId);
        return ResponseHandler.generateResponse(divisionReportModuleResponseDTO, true,
                "Division hierarchy fetched successfully", HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> store(@Valid @RequestBody ReportModuleCreateDTO reportModuleCreateDTO) {
        ReportModuleResponseDTO reportModuleResponseDTO = reportModuleService.createReportModule(reportModuleCreateDTO);
        return ResponseHandler.generateResponse(reportModuleResponseDTO, true, "Module Created successfully",
                HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> getReportModuleById(@Valid @RequestParam String moduleId,
            @RequestBody ReportModuleUpdateDTO reportModuleUpdateDTO) {
        ReportModuleResponseDTO responseDTO = reportModuleService.updateReportModule(moduleId, reportModuleUpdateDTO);
        return ResponseHandler.generateResponse(responseDTO, true, "Module updated successfully", HttpStatus.OK);
    }

    @PostMapping("/create-hierarchy-modules")
    public ResponseEntity<Map<String, Object>> createHierarchyModule(
            @Valid @RequestBody DivisionCreateReportModule divisionCreateReportModule) {
        log.info("hier {}", divisionCreateReportModule);
        String message = reportModuleService.createDivisionHierarchyReportModule(
                divisionCreateReportModule.divisionId(), divisionCreateReportModule.reportModules());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @PutMapping("/update-hierarchy-single-module")
    public ResponseEntity<Map<String, Object>> updateSingleReportModule(@Valid @RequestParam String divisionId) {
        String message = reportModuleService.updateSingleDivisionReportModule(divisionId, new UserReportModuleModel());
        return ResponseHandler.generateResponse(message, true, HttpStatus.OK);
    }

    @DeleteMapping("/destroy")
    public ResponseEntity<Map<String, Object>> deleteModule(@Valid @RequestParam String moduleId) {
        Boolean module = reportModuleService.destroyModule(moduleId);
        return ResponseHandler.generateResponse("Module deleted successfully", module, HttpStatus.OK);
    }

    @PostMapping("/update-module-master")
    public ResponseEntity<Map<String, Object>> updateModuleMaster(
            @Valid @RequestBody ModuleMasterCreateDTO moduleMasterCreateDTO) {
        ModuleMasterEntity entity = reportModuleService.updateModuleMaster(moduleMasterCreateDTO);
        return ResponseHandler.generateResponse(entity, "Module fetched successfully", HttpStatus.OK);
    }

}
