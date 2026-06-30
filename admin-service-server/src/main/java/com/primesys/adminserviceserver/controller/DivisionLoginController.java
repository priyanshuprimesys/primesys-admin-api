package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicecommon.dto.DivisionLoginModuleDTO;
import com.primesys.adminservicecommon.dto.division.DivisionListDTO;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.*;
import com.primesys.adminservicemongodb.model.DeviceTestReport;
import com.primesys.adminserviceserver.request.UpdateModulesRequest;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import com.primesys.adminserviceserver.service.BeatService;
import com.primesys.adminserviceserver.service.DivisionLoginService;
import com.primesys.adminserviceserver.service.ExcelService;
import com.primesys.adminserviceserver.service.impl.ExcelUnlockedColumnsChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.coyote.Response;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/v2/division-logins")
@RequiredArgsConstructor
public class DivisionLoginController {
    private final DivisionLoginService divisionLoginService;

    @GetMapping("/get-all-division")
    public HttpApiResponse<List<DivisionLoginEntity>> getAllDivisionLogins() {

        List<DivisionLoginEntity> divisionLogins = divisionLoginService.getAllDivisionLogins();

        List<DivisionLoginEntity> filtered = divisionLogins.stream()
                .filter(e -> e.getRoleId() != null && (e.getRoleId() == 17 || e.getRoleId() == 7))
                .collect(Collectors.toList());

        return new HttpApiResponse<>(filtered);
    }

    @GetMapping("/division-list")
    public ResponseEntity<Map<String, Object>> getAllDivisionModules() {
        List<DivisionListDTO> divisionListDTOS = divisionLoginService.getAllDivisionList();
        return ResponseHandler.generateResponse(divisionListDTOS, true, "All Divisions Fetched successfully",
                HttpStatus.OK);
    }

    @GetMapping("/get-support-user")
    public HttpApiResponse<List<DivisionLoginEntity>> getAdminLogins() {
        List<DivisionLoginEntity> divisionLogins = divisionLoginService.getAdminLogins();
        HttpApiResponse<List<DivisionLoginEntity>> result = new HttpApiResponse<>(divisionLogins);
        return result;
    }

    @GetMapping("/get-track-user")
    public HttpApiResponse<List<DivisionLoginEntity>> getTrackUserLogins() {
        List<DivisionLoginEntity> divisionLogins = divisionLoginService.getTrackUserLogins();
        HttpApiResponse<List<DivisionLoginEntity>> result = new HttpApiResponse<>(divisionLogins);
        return result;
    }

    @GetMapping("/get-division-details")
    public HttpApiResponse<DivisionLoginEntity> getDivisionDetails(@RequestParam("userName") String userName,
            @RequestParam("password") String password) {
        DivisionLoginEntity divisionLogins = divisionLoginService.getDivisionDetails(userName, password);
        HttpApiResponse<DivisionLoginEntity> result = new HttpApiResponse<>(divisionLogins);
        return result;
    }

    @PutMapping("/update-device-hierarchy-list")
    public HttpApiResponse<DivisionLoginEntity> updateDeviceList(@RequestParam("deviceList") String deviceList,
            @RequestParam("divisionId") String divisionId) {
        DivisionLoginEntity divisionLogins = divisionLoginService.updateDeviceList(deviceList, divisionId);
        HttpApiResponse<DivisionLoginEntity> result = new HttpApiResponse<>(divisionLogins);
        return result;
    }

    @PutMapping()
    public HttpApiResponse<DivisionLoginEntity> updateDivisionLogin(@RequestBody DivisionLoginEntity divisionLogin) {
        DivisionLoginEntity divisionLogins = divisionLoginService.updateDivisionLogin(divisionLogin);

        if (divisionLogins != null) {
            HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(divisionLogins, Boolean.TRUE);
            return httpApiResponse;
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return httpApiResponse;
    }

    /// @author: Priyanshu Chourasia
    /// This method in this controller has been made deprecated by Priyanshu Chourasia
    /// This has been discontinued from 4 March 2026 and methods and services moved to modules folder
    /// to be more modular in this
    // @Deprecated
    @PostMapping()
    public ResponseEntity<HttpApiResponse<DivisionLoginEntity>> createDivisionLogin(
            @RequestBody DivisionLoginEntity divisionLogin) {
        DivisionLoginEntity createdDivisionLogin = divisionLoginService.createDivisionLogin(divisionLogin);
        HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(createdDivisionLogin,
                Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.CREATED);
    }

    // @PostMapping("/upload-beat-file")
    // public ResponseEntity<HttpApiResponse<Object>> handleFileUpload(@RequestPart("file") MultipartFile file) {
    // log.info("upload-beat-file call");
    // if (file.isEmpty()) {
    // ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EMPTY_FILE);
    // HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
    // return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    // }
    // try {
    // List<List<String>> excelData = excelService.readExcelData(file.getInputStream());
    // Optional<FileUploadResultResponse> fileUploadResultResponse = beatService.createBeat(excelData);
    // return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE), HttpStatus.OK);
    // } catch (Exception e) {
    // log.error("Error while processing a file : {} :: error message : {}", file.getOriginalFilename(),
    // e.getMessage());
    // ErrorResponse errorResponse = new ErrorResponse(ErrorCode.FILE_PROCESSING_FAILED);
    // HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
    // return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    // }
    // }

    @GetMapping("/get-division-parents")
    public HttpApiResponse<List<DivisionLoginEntity>> getDivParents(@RequestParam("division_id") String divisionId) {
        List<DivisionLoginEntity> divisionLogins = divisionLoginService.getDivParents(divisionId);
        HttpApiResponse<List<DivisionLoginEntity>> result = new HttpApiResponse<>(divisionLogins);
        return result;
    }

    @PostMapping("/run-report")
    public ResponseEntity<HttpApiResponse<String>> runReport(@RequestBody Map<String, Object> requestBody) {
        String apiUrl = "http://143.244.136.184:5000/run-report"; // Replace with actual server IP

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(response.getBody(), Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        } catch (HttpClientErrorException e) { // 4XX Errors
            e.printStackTrace();
            // return ResponseEntity.status(e.getStatusCode()).body("Client Error: " + e.getResponseBodyAsString());
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(e.getResponseBodyAsString(), Boolean.FALSE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        } catch (HttpServerErrorException e) { // 5XX Errors
            e.printStackTrace();
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(e.getResponseBodyAsString(), Boolean.FALSE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.BAD_GATEWAY);

            // return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Server Error: " +
            // e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();

            // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error: " +
            // e.getMessage());
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(e.getMessage(), Boolean.FALSE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    @PostMapping("/run-recalculate-rdps")
    public ResponseEntity<HttpApiResponse<String>> runRecalculateRdps(@RequestBody Map<String, Object> requestBody) {
        String apiUrl = "http://143.244.136.184:5000/run-recalculate-rdps"; // Replace with actual server IP

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(response.getBody(), Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        } catch (HttpClientErrorException e) { // 4XX Errors
            e.printStackTrace();
            // return ResponseEntity.status(e.getStatusCode()).body("Client Error: " + e.getResponseBodyAsString());
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(e.getResponseBodyAsString(), Boolean.FALSE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        } catch (HttpServerErrorException e) { // 5XX Errors
            e.printStackTrace();
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(e.getResponseBodyAsString(), Boolean.FALSE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.BAD_GATEWAY);

            // return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Server Error: " +
            // e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();

            // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected Error: " +
            // e.getMessage());
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(e.getMessage(), Boolean.FALSE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    @GetMapping("/get-fcm-notification")
    public HttpApiResponse<List<FcmNotificationEntity>> getFcmNotification(
            @RequestParam("division_id") String divisionId) {
        List<FcmNotificationEntity> divisionLogins = divisionLoginService.getFcmNotification(divisionId);
        HttpApiResponse<List<FcmNotificationEntity>> result = new HttpApiResponse<>(divisionLogins);
        return result;
    }

    @PatchMapping("/add-fcm-token")
    public ResponseEntity<HttpApiResponse<DivisionLoginEntity>> addFcmToken(
            @RequestParam("divisionId") String divisionId, @RequestParam("token") String token,
            @RequestParam("updatedBy") String updatedBy) {
        log.info("add-fcm-token-" + token);
        Optional<DivisionLoginEntity> beatEntity = divisionLoginService.addFcmToken(divisionId, token, updatedBy);
        if (beatEntity.isPresent()) {
            HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(beatEntity.get(),
                    Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/remove-fcm-token")
    public ResponseEntity<HttpApiResponse<Object>> removeFcmToken(@RequestParam String divisionId,
            @RequestParam String token) {
        try {
            boolean success = divisionLoginService.removeFcmToken(divisionId, token);
            if (success) {
                HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>("FCM token removed successfully.",
                        Boolean.TRUE);
                return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(500, e.getMessage());

            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

    }

    @PatchMapping("/assign_module")
    public ResponseEntity<HttpApiResponse<DivisionLoginEntity>> assignModule(
            @RequestParam("divisionId") String divisionId, @RequestParam("token") String token,
            @RequestParam("updatedBy") String updatedBy) {
        log.info("add-fcm-token-" + token);
        Optional<DivisionLoginEntity> beatEntity = divisionLoginService.addFcmToken(divisionId, token, updatedBy);
        if (beatEntity.isPresent()) {
            HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(beatEntity.get(),
                    Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PatchMapping("/modules")
    public ResponseEntity<HttpApiResponse<DivisionLoginEntity>> patchModulesList(
            @RequestBody UpdateModulesRequest request) {

        Optional<DivisionLoginEntity> updatedEntity = divisionLoginService.patchModulesList(request.getDivisionId(),
                request.getModulesList(), request.getModifiedBy());

        if (updatedEntity.isPresent()) {
            HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(updatedEntity.get(),
                    Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }

        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<DivisionLoginEntity> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    // ✅ Add or update report for a division
    @PostMapping("/save-device-inspection-report")
    public ResponseEntity<HttpApiResponse<GpsTrackerReport>> saveInspectionReport(
            @RequestBody GpsTrackerReport report) {
        Optional<GpsTrackerReport> entity = divisionLoginService.saveInspectionReport(report);
        if (entity.isPresent()) {
            HttpApiResponse<GpsTrackerReport> httpApiResponse = new HttpApiResponse<>(entity.get(), Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<GpsTrackerReport> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @GetMapping("/get-all-device-inspection-report")
    public ResponseEntity<HttpApiResponse<List<GpsTrackerReport>>> getAllInspectionReport() {
        Optional<List<GpsTrackerReport>> entity = divisionLoginService.getAllInspectionReport();
        if (entity.isPresent()) {
            HttpApiResponse<List<GpsTrackerReport>> httpApiResponse = new HttpApiResponse<>(entity.get(), Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<List<GpsTrackerReport>> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @GetMapping("/get-division-device-inspection-report")
    public ResponseEntity<HttpApiResponse<List<GpsTrackerReport>>> getDivisionInspectionReport(
            @RequestParam("divisionId") String divisionId) {
        Optional<List<GpsTrackerReport>> entity = divisionLoginService.getDivisionInspectionReport(divisionId);
        if (entity.isPresent()) {
            HttpApiResponse<List<GpsTrackerReport>> httpApiResponse = new HttpApiResponse<>(entity.get(), Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<List<GpsTrackerReport>> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PostMapping("/add-device-inspection-report-by-id")
    public ResponseEntity<HttpApiResponse<GpsTrackerReport>> addDeviceToExistingReport(
            @RequestParam("reportId") String reportId, @RequestBody List<DeviceTestReport> devices) {

        Optional<GpsTrackerReport> updatedReport = divisionLoginService.addDeviceToReport(reportId, devices);

        if (updatedReport.isPresent()) {
            HttpApiResponse<GpsTrackerReport> success = new HttpApiResponse<>(updatedReport.get(), Boolean.TRUE);
            return new ResponseEntity<>(success, HttpStatus.OK);
        } else {
            ErrorResponse error = new ErrorResponse(ErrorCode.NOT_FOUND);
            HttpApiResponse<GpsTrackerReport> fail = new HttpApiResponse<>(error);
            return new ResponseEntity<>(fail, HttpStatus.NOT_FOUND);
        }
    }

    // // ✅ Delete report
    // @DeleteMapping("/{divisionId}")
    // public ResponseEntity<Void> delete(@PathVariable String divisionId) {
    // repo.deleteById(divisionId);
    // return ResponseEntity.noContent().build();
    // }
    @GetMapping("/get-report-email-master")
    public HttpApiResponse<List<EmailMasterEntity>> getReportEmailMaster() {
        List<EmailMasterEntity> divisionLogins = divisionLoginService.getReportEmailMaster();
        HttpApiResponse<List<EmailMasterEntity>> result = new HttpApiResponse<>(divisionLogins);
        return result;
    }

}
