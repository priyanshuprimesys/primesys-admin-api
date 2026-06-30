package com.primesys.adminserviceserver.modules.report_modules.dtos;

import java.util.List;

public record DivisionReportModuleCreateDTO(String id, String parentId, String moduleName, String displayName,
        String customDisplayName, Integer displayOrder, Integer typeId, Boolean active, Boolean isTripWise,
        List<DivisionReportModuleCreateDTO> subModules) {
}
