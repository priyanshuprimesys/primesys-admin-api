package com.primesys.adminserviceserver.modules.reports.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GetReportLogDTO(@NotBlank(message = "Division Id is required") String divisionId,
        @NotNull(message = "Device type is required") Integer deviceType,
        @NotNull(message = "Report Date is required") Long reportDate) {
}
