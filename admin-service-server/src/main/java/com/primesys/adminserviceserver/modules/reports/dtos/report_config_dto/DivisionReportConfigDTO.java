package com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DivisionReportConfigDTO {
    String name;
    String reportEmailId;
    String reportEmailPassword;
    Boolean reportEnable;
}
