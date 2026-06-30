package com.primesys.adminservicecommon.dto.report;

import jakarta.validation.constraints.NotBlank;

public record ReportPermissionByIdDTO(@NotBlank(message = "Id cannot be null") String id) {
}
