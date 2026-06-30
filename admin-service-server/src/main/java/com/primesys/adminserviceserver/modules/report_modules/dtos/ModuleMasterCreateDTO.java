package com.primesys.adminserviceserver.modules.report_modules.dtos;

public record ModuleMasterCreateDTO(String moduleId, String moduleName, String displayName, Integer typeId,
        Integer displayOrder) {
}
