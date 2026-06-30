package com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DivisionConfigStatusDTO(@NotBlank(message = "Division Id is required") String divisionId,
        @NotNull(message = "Status is required") Boolean activeStatus,
        @NotNull(message = "Status is required") Boolean reportEnable) {
}
