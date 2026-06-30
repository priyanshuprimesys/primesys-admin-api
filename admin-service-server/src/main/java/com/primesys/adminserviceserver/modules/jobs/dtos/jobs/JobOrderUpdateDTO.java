package com.primesys.adminserviceserver.modules.jobs.dtos.jobs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.Instant;

public record JobOrderUpdateDTO(

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata") @FutureOrPresent(message = "You cannot put past date") Instant holdUptoDate) {
}
