package com.primesys.adminserviceserver.modules.reports.dtos;

public record ReportEmailRequestDTO(String divisionId, Integer deviceTypeId, Long reportDate, String userId) {
}
