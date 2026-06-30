package com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport;

import com.primesys.adminservicemongodb.model.UserReportModuleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DivisionReportModuleResponseDTO {
    private String divisionId;
    private String name;
    private String path;
    List<UserReportModuleModel> reportModules;
    List<DivisionReportModuleResponseDTO> subDivisions;
}
