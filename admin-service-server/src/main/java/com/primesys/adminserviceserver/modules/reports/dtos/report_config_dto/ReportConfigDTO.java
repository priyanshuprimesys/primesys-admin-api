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
public class ReportConfigDTO {
    Integer totalDevices;
    Integer activeDevices;
    Integer inActiveDevices;
    Integer activeDivision;
    Integer inActiveDivision;
    Integer reportDisabledDevices;
    Integer reportActiveDevices;
    List<ReportConfigDetailDTO> reportConfigDetail;
}
