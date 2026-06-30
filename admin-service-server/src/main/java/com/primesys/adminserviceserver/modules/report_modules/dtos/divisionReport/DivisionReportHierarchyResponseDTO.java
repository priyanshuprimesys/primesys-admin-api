package com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminserviceserver.modules.report_modules.dtos.ReportModuleResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DivisionReportHierarchyResponseDTO {
    private String divisionId;
    private String name;
    private String path;
    private List<ReportModuleResponseDTO> moduleDetail;
    private List<DivisionReportHierarchyResponseDTO> subDivisions;
}
