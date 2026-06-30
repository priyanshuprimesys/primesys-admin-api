package com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReportDeviceConfigStatusDTO(@NotBlank(message = "Division ID is required") String divisionId,
        @NotNull(message = "Device Type Id is required") Integer deviceTypeId, List<DeviceConfigStatusDTO> devices) {
}
