package com.primesys.adminserviceserver.modules.report_modules.dtos;

import jakarta.validation.constraints.NotBlank;

public record ReportModuleUpdateDTO(String moduleName, String description, String displayName, Integer displayOrder,
        Integer typeId) {
}
