package com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport;

import com.primesys.adminserviceserver.modules.report_modules.dtos.DivisionReportModuleCreateDTO;

import java.util.List;

public record DivisionCreateReportModule(String divisionId, List<DivisionReportModuleCreateDTO> reportModules) {
}
