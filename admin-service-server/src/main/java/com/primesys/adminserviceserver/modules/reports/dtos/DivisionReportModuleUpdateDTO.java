package com.primesys.adminserviceserver.modules.reports.dtos;

import java.util.List;

public record DivisionReportModuleUpdateDTO(String divisionId, List<String> moduleList) {
}
