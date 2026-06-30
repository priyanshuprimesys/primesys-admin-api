package com.primesys.adminserviceserver.modules.jobs.controllers;

import com.primesys.adminserviceserver.modules.jobs.dtos.jobs.JobOrderCreateDTO;
import com.primesys.adminserviceserver.modules.jobs.dtos.jobs.JobOrderDTO;
import com.primesys.adminserviceserver.modules.jobs.schedulers.ReportScheduledJob;
import com.primesys.adminserviceserver.modules.jobs.services.JobOrderService;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/job_orders", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Job Controllers")
public class JobOrderController {
    private final ReportScheduledJob reportScheduledJob;
    private final JobOrderService jobOrderService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getAll() {
        List<JobOrderDTO> jobOrderDTOS = jobOrderService.getAll();
        return ResponseHandler.generateResponse(jobOrderDTOS, true, "Job Orders fetched successfully", HttpStatus.OK);
    }

    @PostMapping("/job-create")
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody JobOrderCreateDTO jobOrderCreateDTO) {
        JobOrderDTO jobOrderDTO = jobOrderService.create(jobOrderCreateDTO);
        String message = "Job Order created successfully";
        return ResponseHandler.generateResponse(jobOrderDTO, true, message, HttpStatus.CREATED);
    }

}
