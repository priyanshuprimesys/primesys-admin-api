package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicecommon.dto.DeviceCommandDto;
import com.primesys.adminservicecommon.dto.DeviceCommandHistoryDto;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.DeviceCommandHistoryEntity;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.DeviceCommandService;
import com.primesys.adminserviceserver.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/v2/device-command")
@RequiredArgsConstructor
public class DeviceCommandController {

    private final DeviceCommandService deviceCommandService;
    private final ExcelService excelService;

    @GetMapping()
    public ResponseEntity<HttpApiResponse<List<DeviceCommandDto>>> getAllDeviceCommands() {
        List<DeviceCommandDto> deviceCommands = deviceCommandService.getAllDeviceCommands();
        if (CollectionUtils.isEmpty(deviceCommands)) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
            HttpApiResponse<List<DeviceCommandDto>> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        HttpApiResponse<List<DeviceCommandDto>> httpApiResponse = new HttpApiResponse<>(deviceCommands, Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @GetMapping("/history")
    public ResponseEntity<HttpApiResponse<List<DeviceCommandHistoryDto>>> getDeviceCommandHistory(
            @RequestParam long startTime, @RequestParam long endTime) {
        List<DeviceCommandHistoryDto> deviceCommands = deviceCommandService.getAllDeviceCommandHistoryForDate(startTime,
                endTime);

        if (CollectionUtils.isEmpty(deviceCommands)) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
            HttpApiResponse<List<DeviceCommandHistoryDto>> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        HttpApiResponse<List<DeviceCommandHistoryDto>> httpApiResponse = new HttpApiResponse<>(deviceCommands,
                Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PostMapping("/send-command")
    public ResponseEntity<HttpApiResponse<List<DeviceCommandHistoryEntity>>> sendCommand(
            @RequestBody List<DeviceCommandHistoryEntity> command) {
        log.info("send-command call--" + command);
        List<DeviceCommandHistoryEntity> beatEntity = deviceCommandService.sendCommand(command);
        HttpApiResponse<List<DeviceCommandHistoryEntity>> httpApiResponse = new HttpApiResponse<>(beatEntity);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PostMapping("/send-command-emergency")
    public ResponseEntity<HttpApiResponse<List<DeviceCommandHistoryEntity>>> sendCommandEmergency(
            @RequestBody List<DeviceCommandHistoryEntity> command) {
        log.info("send-command call--" + command);
        List<DeviceCommandHistoryEntity> beatEntity = deviceCommandService.sendCommandEmergency(command);
        HttpApiResponse<List<DeviceCommandHistoryEntity>> httpApiResponse = new HttpApiResponse<>(beatEntity);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PostMapping("/send-command-bulk")
    public ResponseEntity<HttpApiResponse<Object>> sendCommandBulk(@RequestParam("file") MultipartFile file) {
        log.info("send-command-bulk call");
        if (file.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EMPTY_FILE);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        // Specify the server location where you want to save the file
        // String serverLocation = "D:\\command_upload_data";
        String serverLocation = "/home/command_upload_data";

        String fileName = file.getOriginalFilename().replace(".xlsx", "") + "_" + System.currentTimeMillis();

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
            FileInputStream fileInputStream = new FileInputStream(serverLocation + File.separator + fileName + ".xlsx");
            String errorInExcel = excelService
                    .validateSendCommandFile(serverLocation + File.separator + fileName + ".xlsx");
            log.info("error----" + errorInExcel);
            if (errorInExcel.length() == 0) {
                List<DeviceCommandHistoryEntity> excelData = excelService.readSendCommandExcelData(fileInputStream);
                log.info("excelData----" + excelData.size());
                List<DeviceCommandHistoryEntity> fileUploadResultResponse = deviceCommandService.sendCommand(excelData);
                return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE),
                        HttpStatus.OK);
            } else {
                ErrorResponse errorResponse = new ErrorResponse(1005, errorInExcel);
                HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
                return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
            }
            //
        } catch (Exception e) {
            log.error("Error while processing a file : {} :: error message : {}", file.getOriginalFilename(),
                    e.getMessage());
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(1005, e.getMessage());
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
    }
}
