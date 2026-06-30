package com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportConfigDetailDTO {
    String name;
    String divisionId;
    String userName;
    Integer totalDevices;
    Integer totalReportModule;
    Integer activeDevices;
    Integer reportEnabled;
    Integer inActiveDevices;
    Integer reportDisabled;
}
