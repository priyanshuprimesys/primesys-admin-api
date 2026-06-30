package com.primesys.adminserviceserver.modules.jobs.dtos.jobs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.primesys.adminservicemongodb.enums.JobOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

public record JobOrderCreateDTO(@NotBlank(message = "Job Order name is required") String jobName,

        @NotNull(message = "Status is required") JobOrderStatus status,

        List<String> trackDivisionIds,

        String typeId,

        @NotNull(message = "Please select a date to start job") @FutureOrPresent(message = "You cannot put a date which is past") @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Kolkata") LocalDateTime startDateFrom,

        @NotNull(message = "Please select a date upto which job will run") @FutureOrPresent(message = "You cannot put date which is in past") @JsonFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endDateAt,

        @NotBlank(message = "Created by is required") String createdBy) {
}
