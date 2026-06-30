package com.primesys.adminserviceserver.dtos.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record DeleteLocationRequestDTO(@NotNull(message = "Imei cannot be null") Long imei,
        @NotNull(message = "Timestamps cannot be null") List<Long> timestamp,
        @NotBlank(message = "User Id cannot be null") String divisionId) {
}
