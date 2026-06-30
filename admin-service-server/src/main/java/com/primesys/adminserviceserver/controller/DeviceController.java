package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicecommon.dto.DeviceDto;
import com.primesys.adminservicecommon.dto.DeviceExchangeDTO;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.entity.DeviceInfoMaster;
import com.primesys.adminservicemongodb.entity.DeviceTypeMasterEntity;
import com.primesys.adminserviceserver.request.DeviceRequest;
import com.primesys.adminserviceserver.request.RenewDeviceRequest;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import com.primesys.adminserviceserver.service.BeatService;
import com.primesys.adminserviceserver.service.DeviceService;
import com.primesys.adminserviceserver.service.DeviceTypeMasterService;
import com.primesys.adminserviceserver.service.ExcelService;
import com.primesys.adminserviceserver.service.impl.ExcelUnlockedColumnsChecker;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/v2/device")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    private final ExcelService excelService;
    private final BeatService beatService;
    private final DeviceTypeMasterService deviceTypeMasterService;

    @GetMapping("/all")
    HttpApiResponse<List<DeviceDto>> getAllDevicesOfDivision(@RequestParam final @NotBlank String divisionId) {
        log.info("all device details called by division id {}", divisionId);

        List<DeviceDto> deviceDtos = deviceService.getAllDevicesOfDivision(divisionId);
        HttpApiResponse<List<DeviceDto>> response = new HttpApiResponse<List<DeviceDto>>(deviceDtos);
        log.info("all device details is {}", response);
        return response;
    }

    @Deprecated(forRemoval = true)
    @GetMapping("/all-device-details")
    HttpApiResponse<List<DeviceEntity>> getAllDevicesWithDetailsOfDivision(
            @RequestParam final @NotBlank String divisionId) {
        Optional<List<DeviceEntity>> deviceDtos = deviceService.getAllDevicesWithDetailsOfDivision(divisionId);
        HttpApiResponse<List<DeviceEntity>> response = new HttpApiResponse<List<DeviceEntity>>(deviceDtos.get());
        return response;
    }

    @GetMapping("/device-details")
    public ResponseEntity<Map<String, Object>> deviceDetails(@RequestParam final String divisionId) {
        List<DeviceEntity> deviceEntities = deviceService.getAllDevicesWithDetails(divisionId);
        return ResponseHandler.generateResponse(deviceEntities, true, deviceEntities.size() + " total devices fetched",
                HttpStatus.OK);
    }

    @GetMapping("/all-devices")
    HttpApiResponse<List<DeviceDto>> getAllDevices() {
        log.info("all-devices details called");

        List<DeviceDto> deviceDtos = deviceService.getAllDevices();
        HttpApiResponse<List<DeviceDto>> response = new HttpApiResponse<List<DeviceDto>>(deviceDtos);
        log.info("all device details is {}", response);
        return response;
    }

    @GetMapping("/get-device-details")
    HttpApiResponse<DeviceEntity> getDevicesDetails(@RequestParam final @NotBlank String deviceImei) {
        log.info("get device details called {}", deviceImei);

        DeviceEntity deviceDtos = deviceService.getDevicesDetails(deviceImei);
        HttpApiResponse<DeviceEntity> response = new HttpApiResponse<>(deviceDtos);
        log.info("get device details called  is {}", response);
        return response;
    }

    @PostMapping()
    public ResponseEntity<HttpApiResponse<Object>> SaveDevice(@RequestBody DeviceRequest device) {
        log.info(device.toString());
        try {
            Optional<DeviceEntity> deviceEntity = deviceService.saveDevice(device);
            if (deviceEntity.isPresent()) {
                HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(deviceEntity.get(), Boolean.TRUE);
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

    @PutMapping()
    public ResponseEntity<HttpApiResponse<DeviceEntity>> updateDevice(@Valid @RequestBody DeviceRequest device) {
        Optional<DeviceEntity> deviceEntity = deviceService.saveDevice(device);
        if (deviceEntity.isPresent()) {
            HttpApiResponse<DeviceEntity> httpApiResponse = new HttpApiResponse<>(deviceEntity.get(), Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<DeviceEntity> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PostMapping("/upload")
    public ResponseEntity<HttpApiResponse<Object>> handleFileUpload(@RequestParam("file") MultipartFile file,
            @RequestParam("device") DeviceRequest deviceRequest,
            @RequestParam("deviceStartSerialNo") int deviceStartSerialNo) {
        log.info("deviceRequest--" + deviceRequest);
        if (file.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EMPTY_FILE);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        try {
            List<List<String>> excelData = excelService.readDeviceExcelData(file.getInputStream());
            Optional<FileUploadResultResponse> fileUploadResultResponse = deviceService.createDevice(excelData,
                    deviceRequest, deviceStartSerialNo);
            return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while processing a file : {} :: error message : {}", file.getOriginalFilename(),
                    e.getMessage());
            // e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(500, e.getMessage());

            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
    }

    @PutMapping("/update-sos-number")
    public ResponseEntity<HttpApiResponse<String>> updateSosNumber(@RequestParam("deviceImei") Long deviceImei,
            @RequestParam("sosNumbers") List<String> sosNumbers) {
        log.info("update-sos-number deviceImei {} sosNumbers {}", deviceImei, sosNumbers);
        Long updated = deviceService.updateSosNumber(deviceImei, sosNumbers);
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(updated + " device(s) SOS number updated.",
                Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PutMapping("/update-device-imei")
    public ResponseEntity<HttpApiResponse<DeviceEntity>> updateDeviceImei(
            @RequestParam("oldDeviceImei") Long oldDeviceImei, @RequestParam("newDeviceImei") Long newDeviceImei) {
        log.info("oldDeviceImei " + oldDeviceImei);
        Optional<DeviceEntity> deviceEntity = deviceService.updateDeviceImei(oldDeviceImei, newDeviceImei);
        if (deviceEntity.isPresent()) {
            HttpApiResponse<DeviceEntity> httpApiResponse = new HttpApiResponse<>(deviceEntity.get(), Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<DeviceEntity> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PutMapping("/exchange-device")
    public ResponseEntity<HttpApiResponse<String>> exchangeDevice(@RequestParam("oldDeviceId") String oldDeviceId,
            @RequestParam("newDeviceId") String newDeviceId, @RequestParam("userLoginId") String userLoginId) {
        log.info("oldDeviceId " + oldDeviceId);
        String deviceExMsg = deviceService.exchangeDevice(oldDeviceId, newDeviceId, userLoginId);
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(deviceExMsg, Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

    }

    @GetMapping("/get-exchange-device")
    public HttpApiResponse<Page<DeviceExchangeDTO>> getExchangeDevice(@RequestParam("userLoginId") String userLoginId,
            Pageable pageable) {

        Page<DeviceExchangeDTO> deviceExchangePage = deviceService.getExchangeDevice(userLoginId, pageable);
        HttpApiResponse<Page<DeviceExchangeDTO>> response = new HttpApiResponse<>(deviceExchangePage);
        log.info("Paginated exchange devices fetched: {}", response);
        return response;
    }

    @PutMapping("/renew-device-division-wise")
    public ResponseEntity<HttpApiResponse<String>> renewDeviceDivisionWise(
            @RequestParam("divisionId") String divisionId, @RequestParam("userLoginId") String userLoginId,
            @RequestParam("days") Integer days) {
        log.info("renew-device divisionId " + divisionId);
        Integer deviceRenew = deviceService.renewDeviceDivisionWise(divisionId, userLoginId, days);
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(deviceRenew + " Devices are renew.",
                Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

    }

    @PutMapping("/renew-device-wise")
    public ResponseEntity<HttpApiResponse<String>> renewDevice(@RequestParam("deviceId") String deviceId,
            @RequestParam("userLoginId") String userLoginId, @RequestParam("days") Integer days) {
        log.info("renew-device deviceId " + deviceId);
        Integer deviceRenew = deviceService.renewDevice(deviceId, userLoginId, days);
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(deviceRenew + " Devices are renew.",
                Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

    }

    @PutMapping("/renew-device-range-wise")
    public ResponseEntity<HttpApiResponse<String>> renewDeviceRange(RenewDeviceRequest renewDeviceRequest) {
        log.info("renew-device deviceId " + renewDeviceRequest);
        Integer deviceRenew = deviceService.renewDeviceRange(renewDeviceRequest);
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(deviceRenew + " Devices are renew.",
                Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

    }

    @PostMapping(value = "/upload-device-update-file", consumes = { "multipart/form-data", "application/json" })
    public ResponseEntity<HttpApiResponse<Object>> uploadDivisionFile(@RequestParam("file") MultipartFile file,
            @RequestParam("updatedBy") String updatedBy) {
        log.info("upload-device-update-file call--");

        if (file.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EMPTY_FILE);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }

        String serverLocation = "/home/device_upload_data";
        // String serverLocation = "D:\\home\\device_upload_data";

        String originalName = file.getOriginalFilename();
        String baseName = FilenameUtils.getBaseName(originalName); // Removes extension safely
        String normalizedBaseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "_"); // Allow only safe characters
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = normalizedBaseName + "_" + timestamp;

        try {
            // Get the file bytes
            // byte[] bytes = file.getBytes();

            // Create the directory if it doesn't exist
            File directory = new File(serverLocation);
            if (!directory.exists()) {
                directory.mkdirs(); // creates parent directories as well
            }

            // Create the file on the server
            File serverFile = new File(directory.getAbsolutePath() + File.separator + fileName + ".xlsx");
            file.transferTo(serverFile);

        } catch (Exception e) {
            log.error("Error while up-loading  a file : {} :: error message : {}", file.getOriginalFilename(),
                    e.getMessage());
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(1005, e.getMessage());
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        try {
            // Create a FileInputStream for the file
            String col = ExcelUnlockedColumnsChecker
                    .getColumnName(serverLocation + File.separator + fileName + ".xlsx");
            if (col != null && !col.equalsIgnoreCase("")) {
                Optional<FileUploadResultResponse> fileUploadResultResponse = deviceService.updateColumn(col,
                        serverLocation + File.separator + fileName + ".xlsx", updatedBy);
                return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE),
                        HttpStatus.OK);

            }
            // FileInputStream fileInputStream = new FileInputStream(serverLocation + File.separator + fileName +
            // ".xlsx");
            // String errorInExcel = excelService
            // .validateDeviceNoBeatFile(serverLocation + File.separator + fileName + ".xlsx");
            // log.info(errorInExcel);
            // if (errorInExcel.length() == 0) {
            // List<List<String>> excelData = excelService.readBeatDeviceNoExcelData(fileInputStream);
            // log.info("excelData----" + excelData.size());
            // log.info("excelData----" + excelData);
            //
            // Optional<FileUploadResultResponse> fileUploadResultResponse = beatService.createBeatDeviceNo(beat,
            // excelData, false, fileName + ".xlsx");
            // return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE),
            // HttpStatus.OK);
            // } else {
            // ErrorResponse errorResponse = new ErrorResponse(1005, errorInExcel);
            // HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            // return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
            // }

        } catch (Exception e) {
            log.error("Error while processing a file : {} :: error message : {}", file.getOriginalFilename(),
                    e.getMessage());
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(1005, e.getMessage());
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        return null;
    }

    @GetMapping("/device-info-full")
    public HttpApiResponse<DeviceInfoMaster> getDeviceInfoByImei(@RequestParam final @NotBlank Long deviceImei) {
        HttpApiResponse<DeviceInfoMaster> response = new HttpApiResponse<DeviceInfoMaster>(
                deviceService.getDeviceInfoByImei(deviceImei));
        log.info("/device-info-full is {}", response);
        return response;

    }

    @GetMapping("/device-types")
    public HttpApiResponse<List<DeviceTypeMasterEntity>> getDeviceTypes() {
        List<DeviceTypeMasterEntity> data = deviceTypeMasterService.getAllDeviceTypes();
        log.info("Fetched device types: {}", data);
        return new HttpApiResponse<>(data);
    }
}
