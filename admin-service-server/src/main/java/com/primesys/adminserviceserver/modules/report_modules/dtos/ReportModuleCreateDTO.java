package com.primesys.adminserviceserver.modules.report_modules.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@AllArgsConstructor
//@NoArgsConstructor
//@Data
public record ReportModuleCreateDTO(String parentId, @NotBlank(message = "Module name is required") String moduleName,
        String description, @NotBlank(message = "Display name is required") String displayName, Integer displayOrder,
        Integer typeId) {
}
