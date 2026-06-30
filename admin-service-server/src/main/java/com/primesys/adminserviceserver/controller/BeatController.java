package com.primesys.adminserviceserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primesys.adminservicemongodb.model.BeatGroupByFileDTO;
import com.primesys.adminservicecommon.dto.DeviceBeatDto;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.BeatEntity;
import com.primesys.adminserviceserver.request.DeviceBeatRequest;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.BeatService;
import com.primesys.adminserviceserver.service.ExcelService;
import com.primesys.adminserviceserver.utility.BeatFileValidator;
import com.primesys.adminserviceserver.utility.BeatTemplateGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/beat")
@CrossOrigin("*")

public class BeatController {
    private final BeatService beatService;
    private final ExcelService excelService;
    private final BeatFileValidator validator;
    private final BeatTemplateGenerator templateGenerator;

    /**
     * Download the beat upload template. GET /v2/beat/download-template Always returns the latest template in sync with
     * the reader/validator.
     */
    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] bytes = templateGenerator.generate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(
                    MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(
                    ContentDisposition.attachment().filename("beats_upload_template.xlsx").build());
            headers.setContentLength(bytes.length);
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generating beat template: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Deprecated
    @PostMapping("/upload-beat")
    public ResponseEntity<HttpApiResponse<List<BeatEntity>>> uploadBeat(@RequestBody BeatEntity beat) {
        log.info("upload-beat call--" + beat);
        List<BeatEntity> beatEntity = beatService.createBeat(beat, false);
        HttpApiResponse<List<BeatEntity>> httpApiResponse = new HttpApiResponse<>(beatEntity);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @Deprecated
    @PostMapping("/upload-beat-multiple-path")
    public ResponseEntity<HttpApiResponse<List<BeatEntity>>> updateBeatMultiplePath(@RequestBody BeatEntity beat) {
        log.info("upload-beat-multiple-path call--" + beat);
        List<BeatEntity> beatEntity = beatService.createBeat(beat, true);
        HttpApiResponse<List<BeatEntity>> httpApiResponse = new HttpApiResponse<>(beatEntity);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/upload-device-no-beat-file", consumes = { "multipart/form-data", "application/json" })
    public ResponseEntity<HttpApiResponse<Object>> uploadDeviceNoBeatFile(@RequestParam("file") MultipartFile file,
            @RequestParam("beat") String beatData,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun) {
        log.info("upload-device-no-beat-file call dryRun={}", dryRun);

        // Convert the JSON string (beatData) to the DeviceBeatRequest object
        ObjectMapper objectMapper = new ObjectMapper();
        DeviceBeatRequest beat = null;
        try {
            beat = objectMapper.readValue(beatData, DeviceBeatRequest.class);
        } catch (IOException e) {
            log.error("Error parsing beat data", e);
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_BEAT_DATA);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.BAD_REQUEST);
        }

        log.info("upload-device-no-beat-file call");
        if (file.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EMPTY_FILE);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }

        // Specify the server location where you want to save the file
        // String serverLocation = "D:\\Beat_upload_data";
        String serverLocation = "/home/beat_upload_data";

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

            FileInputStream fileInputStream = new FileInputStream(serverLocation + File.separator + fileName + ".xlsx");
            String errorInExcel = validator
                    .validateDeviceNoBeatFile(serverLocation + File.separator + fileName + ".xlsx");
            log.info(errorInExcel);
            if (errorInExcel.length() == 0) {
                List<List<String>> excelData = excelService.readBeatDeviceNoExcelData(fileInputStream);
                log.info("excelData----" + excelData.size());
                log.info("excelData----" + excelData);

                Optional<FileUploadResultResponse> fileUploadResultResponse = beatService.createBeatDeviceNo(beat,
                        excelData, false, fileName + ".xlsx", dryRun);
                return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE),
                        HttpStatus.OK);
            } else {
                ErrorResponse errorResponse = new ErrorResponse(1005, errorInExcel);
                HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
                return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
            }

        } catch (Exception e) {
            log.error("Error while processing a file : {} :: error message : {}", file.getOriginalFilename(),
                    e.getMessage());
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(1005, e.getMessage());
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/upload-hourly-beat-file", consumes = { "multipart/form-data", "application/json" })
    public ResponseEntity<HttpApiResponse<Object>> uploadHourlyReportBeatFile(@RequestParam("file") MultipartFile file,
            @RequestParam("beat") String beatData) {
        log.info("upload-device-no-beat-file call--");

        // Convert the JSON string (beatData) to the DeviceBeatRequest object
        ObjectMapper objectMapper = new ObjectMapper();
        DeviceBeatRequest beat = null;
        try {
            beat = objectMapper.readValue(beatData, DeviceBeatRequest.class);
        } catch (IOException e) {
            log.error("Error parsing beat data", e);
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_BEAT_DATA);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.BAD_REQUEST);
        }

        log.info("upload-device-no-beat-file call");
        if (file.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EMPTY_FILE);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }

        // Specify the server location where you want to save the file
        // String serverLocation = "D:\\Beat_upload_data";
        String serverLocation = "/home/beat_upload_data";

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

            FileInputStream fileInputStream = new FileInputStream(serverLocation + File.separator + fileName + ".xlsx");
            String errorInExcel = validator
                    .validateDeviceNoBeatFile(serverLocation + File.separator + fileName + ".xlsx");
            log.info(errorInExcel);
            if (errorInExcel.length() == 0) {
                List<List<String>> excelData = excelService.readBeatDeviceNoExcelData(fileInputStream);
                log.info("excelData----" + excelData.size());
                log.info("excelData----" + excelData);

                Optional<FileUploadResultResponse> fileUploadResultResponse = beatService.createBeatHourly(beat,
                        excelData, false, fileName + ".xlsx");
                return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE),
                        HttpStatus.OK);
            } else {
                ErrorResponse errorResponse = new ErrorResponse(1005, errorInExcel);
                HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
                return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
            }

        } catch (Exception e) {
            log.error("Error while processing a file : {} :: error message : {}", file.getOriginalFilename(),
                    e.getMessage());
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(1005, e.getMessage());
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
    }

    @GetMapping("/get-device-beat")
    HttpApiResponse<List<DeviceBeatDto>> getDeviceBeat(@RequestParam("deviceImei") Long deviceImei) {
        log.info("get-device-beat call--" + deviceImei);

        final List<DeviceBeatDto> deviceDtos = beatService.getDeviceBeat(deviceImei);
        final HttpApiResponse<List<DeviceBeatDto>> response = new HttpApiResponse<>(deviceDtos);
        log.info("get-device-beat is {}", response);
        return response;
    }

    @GetMapping("/get-device-type-beat")
    HttpApiResponse<List<DeviceBeatDto>> getDeviceTypeBeat(@RequestParam("divisionId") String divisionId,
            @RequestParam("deviceType") Integer deviceType) {
        log.info("get-device-type-beat call--" + divisionId);

        final List<DeviceBeatDto> deviceDtos = beatService.getDeviceTypeBeat(divisionId, deviceType);
        final HttpApiResponse<List<DeviceBeatDto>> response = new HttpApiResponse<>(deviceDtos);
        log.info("get-device-beat is {}", response);
        return response;
    }

    @PostMapping("/upload-single-device-beat")
    public ResponseEntity<HttpApiResponse<Object>> uploadSingleBeat(@RequestBody DeviceBeatRequest beat) {
        log.info("upload-single-device-beat-" + beat);
        List<List<String>> excelData = new ArrayList<>();
        List<String> rowData = new ArrayList<>();
        rowData.add(beat.getDeviceName());
        rowData.add(String.valueOf(beat.getDeviceNo()));
        rowData.add(beat.getSectionName());
        rowData.add(String.valueOf(beat.getDeviceTypeId()));
        rowData.add(String.valueOf(beat.getTstartKm()));
        rowData.add(String.valueOf(beat.getTendKm()));
        if (!(beat.getTstartKm() == 0 && beat.getTendKm() == 0)) {
            rowData.add(beat.getStartTime());
            rowData.add(beat.getEndTime());

        }
        log.info(String.valueOf(rowData));
        excelData.add(rowData);

        Optional<FileUploadResultResponse> fileUploadResultResponse = beatService.createBeatDeviceNo(beat, excelData,
                beat.getIsMultipleBeatPath(), "single_beat_insert_" + System.currentTimeMillis(), false);
        return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE), HttpStatus.OK);
    }

    @PostMapping("/add-beat-manual")
    public ResponseEntity<HttpApiResponse<Object>> addBeatManual(@RequestBody DeviceBeatRequest beat,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun) {
        log.info("add-beat-manual call-- {}", beat);
        Optional<FileUploadResultResponse> result = beatService.createBeatManual(beat, dryRun);
        return new ResponseEntity<>(new HttpApiResponse<>(result, Boolean.TRUE), HttpStatus.OK);
    }

    @PutMapping("/update-device-beat")
    public ResponseEntity<HttpApiResponse<Object>> updateBeat(@RequestBody DeviceBeatRequest beat) {
        log.info("update-device-beat-" + beat);

        Optional<BeatEntity> response = beatService.updateBeat(beat);
        return new ResponseEntity<>(new HttpApiResponse<>(response, Boolean.TRUE), HttpStatus.OK);
    }

    @PatchMapping("/delete-device-beat")
    public ResponseEntity<HttpApiResponse<BeatEntity>> deleteBeat(@RequestParam("beatId") String beatId,
            @RequestParam("updatedBy") String updatedBy) {
        log.info("delete-device-beat-" + beatId);
        Optional<BeatEntity> beatEntity = beatService.deleteBeat(beatId, updatedBy);
        if (beatEntity.isPresent()) {
            HttpApiResponse<BeatEntity> httpApiResponse = new HttpApiResponse<>(beatEntity.get(), Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<BeatEntity> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PatchMapping("/approve-device-beat")
    public ResponseEntity<HttpApiResponse<List<BeatEntity>>> approveMultipleBeats(@RequestParam("beatId") String beatId,
            @RequestParam("updatedBy") String updatedBy) {
        log.info("approveBeat-device-beat-" + beatId);
        List<BeatEntity> beatEntity = beatService.approveMultipleBeats(beatId, updatedBy);
        HttpApiResponse<List<BeatEntity>> httpApiResponse = new HttpApiResponse<>(beatEntity, Boolean.TRUE);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @GetMapping("/unapproved/grouped-by-ref-file")
    public ResponseEntity<HttpApiResponse<List<BeatGroupByFileDTO>>> getUnapprovedGroupedByRefFile() {
        log.info("Fetching unapproved beats grouped by ref file");

        List<BeatGroupByFileDTO> beatEntity = beatService.getUnapprovedGroupedByRefFile();

        if (!beatEntity.isEmpty()) {
            HttpApiResponse<List<BeatGroupByFileDTO>> successResponse = new HttpApiResponse<>(beatEntity, true);
            return ResponseEntity.ok(successResponse);
        }

        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<List<BeatGroupByFileDTO>> errorApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(errorApiResponse, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete-device-beat-approval-file")
    public ResponseEntity<HttpApiResponse<String>> deleteBeataApprovalFile(
            @RequestParam("refFileName") String refFileName, @RequestParam("updatedBy") String updatedBy) {
        log.info("deleteBeataApprovalFile-" + refFileName);
        Optional<Integer> beatEntity = beatService.deleteBeataApprovalFile(refFileName, updatedBy);
        if (beatEntity.isPresent()) {
            HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(
                    "Deleted " + beatEntity.get().intValue() + " records.", Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(errorResponse);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

}
