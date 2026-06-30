package com.primesys.adminserviceserver.modules.division.controllers;

import com.primesys.adminserviceserver.modules.division.dtos.DivisionStatsDTO;
import com.primesys.adminserviceserver.modules.division.services.DivisionStatsService;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/division-stats")
@RequiredArgsConstructor
@Tag(name = "Division Stats Controller")
public class DivisionStatsController {

    private final DivisionStatsService divisionStatsService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAllStats() {
        DivisionStatsDTO statsDTO = divisionStatsService.getDivisionStats();
        return ResponseHandler.generateResponse(statsDTO, true, "Division stats fetched successfully", HttpStatus.OK);
    }

}
