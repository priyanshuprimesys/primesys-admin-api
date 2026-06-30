package com.primesys.adminserviceserver.dtos.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record DeviceLocationRequestDTO(
        @NotNull(message = "Imei cannot be null") @Positive(message = "Imei must be positive") Long imei,

        @NotNull(message = "Start time cannot be null") @Positive(message = "Start time must be positive") Long startTime,

        @NotNull(message = "End time cannot be null") @Positive(message = "End time must be positive") Long endTime) {
}
