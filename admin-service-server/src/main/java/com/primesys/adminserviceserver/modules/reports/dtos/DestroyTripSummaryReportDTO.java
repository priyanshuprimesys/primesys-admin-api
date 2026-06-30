package com.primesys.adminserviceserver.modules.reports.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DestroyTripSummaryReportDTO(@NotBlank(message = "division id is required") String divisionId,
        @NotNull(message = "device type is required") Integer deviceType,
        @NotNull(message = "Report date is required") Long reportDate) {
}
