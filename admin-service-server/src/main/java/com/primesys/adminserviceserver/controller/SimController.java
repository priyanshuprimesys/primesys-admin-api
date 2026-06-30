package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.SimEntity;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.response.SimUploadResult;
import com.primesys.adminserviceserver.service.SimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/sim")
@CrossOrigin("*")
public class SimController {

    private final SimService simService;

    /**
     * Bulk-import SIM data from a CSV or Excel file. Columns are matched by header, so the Jio export (ICCID / IMSI /
     * MSISDN / IMEI) and the Airtel export (SIM_NO / SIM_IMSI / MOBILE_NUMBER / IMEI / BASKET_NAME / SIM_STATUS /
     * PLAN_NAME / ACTIVATION_DATE / ONBOARDING_DATE / APN1) both map onto the same record. The {@code simProvider} you
     * pass is stored as {@code sim_provider}.
     *
     * POST /v1/sim/upload?simProvider=JIO&createdBy=admin (multipart field: file)
     */
    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })
    public ResponseEntity<HttpApiResponse<Object>> uploadSimFile(@RequestParam("file") MultipartFile file,
            @RequestParam("simProvider") String simProvider,
            @RequestParam(value = "createdBy", required = false) String createdBy) {
        log.info("sim/upload call simProvider={} file={}", simProvider, file.getOriginalFilename());

        if (file.isEmpty()) {
            HttpApiResponse<Object> response = new HttpApiResponse<>(new ErrorResponse(ErrorCode.EMPTY_FILE));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        try {
            SimUploadResult result = simService.importSimFile(file, simProvider, createdBy);
            return new ResponseEntity<>(new HttpApiResponse<>(result, Boolean.TRUE), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid SIM upload: {}", e.getMessage());
            HttpApiResponse<Object> response = new HttpApiResponse<>(new ErrorResponse(1005, e.getMessage()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error processing SIM file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
            HttpApiResponse<Object> response = new HttpApiResponse<>(new ErrorResponse(1005, e.getMessage()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    /**
     * Fetch all SIM records, optionally filtered by provider.
     *
     * GET /v1/sim → every SIM (Jio + Airtel) GET /v1/sim?simProvider=JIO → only Jio GET /v1/sim?simProvider=AIRTEL →
     * only Airtel
     */
    @GetMapping
    public ResponseEntity<HttpApiResponse<List<SimEntity>>> getSims(
            @RequestParam(value = "simProvider", required = false) String simProvider) {
        log.info("sim/get call simProvider={}", simProvider);
        List<SimEntity> records = simService.getSimRecords(simProvider);
        return new ResponseEntity<>(new HttpApiResponse<>(records, Boolean.TRUE), HttpStatus.OK);
    }
}
