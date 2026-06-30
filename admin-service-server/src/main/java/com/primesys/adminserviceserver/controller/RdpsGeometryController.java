package com.primesys.adminserviceserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.entity.RdpsGeometryEntity;
import com.primesys.adminserviceserver.request.DeviceRequest;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.RdpsGeometryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/rdps")
@CrossOrigin("*")

public class RdpsGeometryController {
    private final RdpsGeometryService rdpsGeometryService;

    @PostMapping("/save-rdps")
    public ResponseEntity<HttpApiResponse<List<RdpsGeometryEntity>>> saveRdps(
            @RequestBody List<RdpsGeometryEntity> rdps) {
        log.info("save-rdps call--" + rdps);
        List<RdpsGeometryEntity> rdpsEntity = rdpsGeometryService.saveRdps(rdps);
        HttpApiResponse<List<RdpsGeometryEntity>> httpApiResponse = new HttpApiResponse<>(rdpsEntity);
        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    // @GetMapping("/get-rdps-div")
    // HttpApiResponse<List<DeviceBeatDto>> getRdps(@RequestParam("division_id") Long divisionId) {
    // log.info("get-getRdps call--" + divisionId);
    //
    // final List<DeviceBeatDto> deviceDtos = rdpsGeometryService.getDeviceBeat(deviceImei);
    // final HttpApiResponse<List<DeviceBeatDto>> response = new HttpApiResponse<>(deviceDtos);
    // log.info("get-device-beat is {}", response);
    // return response;
    // }

    // PutMapping for updating an existing RdpsGeometryEntity
    @PutMapping()
    public ResponseEntity<HttpApiResponse<RdpsGeometryEntity>> updateRdpsGeometry(
            @RequestBody RdpsGeometryEntity updatedRdpsGeometry) {

        // Check if the entity exists in the database
        Optional<RdpsGeometryEntity> existingEntityOptional = rdpsGeometryService.findById(updatedRdpsGeometry.getId());

        if (existingEntityOptional.isPresent()) {

            RdpsGeometryEntity existingEntity = existingEntityOptional.get();

            // Update fields of the existing entity with the new values from the request
            existingEntity.setGeoLocation(updatedRdpsGeometry.getGeoLocation());
            existingEntity.setKilometer(updatedRdpsGeometry.getKilometer());
            existingEntity.setDistance(updatedRdpsGeometry.getDistance());
            existingEntity.setFeatureCode(updatedRdpsGeometry.getFeatureCode());
            existingEntity.setLatitude(updatedRdpsGeometry.getLatitude());
            existingEntity.setLongitude(updatedRdpsGeometry.getLongitude());
            existingEntity.setDivisionId(updatedRdpsGeometry.getDivisionId());
            existingEntity.setActiveStatus(updatedRdpsGeometry.getActiveStatus());
            existingEntity.setApprovedStatus(updatedRdpsGeometry.getApprovedStatus());
            existingEntity.setFeatureImage(updatedRdpsGeometry.getFeatureImage());
            existingEntity.setFeatureDetail(updatedRdpsGeometry.getFeatureDetail());
            existingEntity.setSection(updatedRdpsGeometry.getSection());
            existingEntity.setUpdatedAt(updatedRdpsGeometry.getUpdatedAt());
            existingEntity.setUpdatedBy(updatedRdpsGeometry.getUpdatedBy());

            // Save the updated entity
            RdpsGeometryEntity updatedEntity = rdpsGeometryService.save(existingEntity);

            HttpApiResponse<RdpsGeometryEntity> httpApiResponse = new HttpApiResponse<>(updatedEntity, Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        } else {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.NOT_FOUND);
            HttpApiResponse<RdpsGeometryEntity> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/upload-rdps-file", consumes = { "multipart/form-data", "application/json" })
    public ResponseEntity<HttpApiResponse<Object>> uploadDeviceNoBeatFile(@RequestParam("file") MultipartFile file,
            @RequestParam("divisionId") String divisionId) {

        log.info("upload-rdps-file call");
        if (file.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EMPTY_FILE);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }
        // Get the original filename and check for the .csv extension
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_FILE_TYPE);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }

        // Specify the server location where you want to save the file
        // String serverLocation = "D:\\rdps_upload_data";
        String serverLocation = "/home/rdps_upload_data";

        String fileName = file.getOriginalFilename().replace(".csv", "") + "_" + System.currentTimeMillis();

        try {
            // Create the directory if it doesn't exist
            File directory = new File(serverLocation);
            if (!directory.exists()) {
                directory.mkdirs(); // creates parent directories as well
            }

            // Create the file on the server
            File serverFile = new File(directory.getAbsolutePath() + File.separator + fileName + ".csv");
            file.transferTo(serverFile);

            Optional<String> fileUploadResultResponse = rdpsGeometryService
                    .uploadRdpsFile(serverLocation + File.separator + fileName + ".csv", divisionId);
            return new ResponseEntity<>(new HttpApiResponse<>(fileUploadResultResponse, Boolean.TRUE), HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error while up-loading  a file : {} :: error message : {}", file.getOriginalFilename(),
                    e.getMessage());
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(1005, e.getMessage());
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }

    }

    @GetMapping("/division-rdps-data")
    HttpApiResponse<List<RdpsGeometryEntity>> getDivisionRdpsData(@RequestParam final String divisionId) {
        log.info("got device division-rdps-data request {}", divisionId);
        final List<RdpsGeometryEntity> rdpsDtoList = rdpsGeometryService.getDivisionRdpsData(divisionId);
        final HttpApiResponse<List<RdpsGeometryEntity>> response = new HttpApiResponse<>(rdpsDtoList);
        // log.info("got device rdps-dat a response {}", response);
        return response;
    }

    @PatchMapping("/delete")
    public ResponseEntity<HttpApiResponse<String>> deleteRdpsData(@RequestParam final String rdpsId) {
        log.info("Received request to soft delete rdps data with rdpsId: {}", rdpsId);

        // Perform the soft delete operation and get the result message
        String resultMessage = rdpsGeometryService.deleteRdpsData(rdpsId);

        // Prepare the response with the result message
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(resultMessage, Boolean.TRUE);
        log.info("Soft delete response: {}", resultMessage);

        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

}