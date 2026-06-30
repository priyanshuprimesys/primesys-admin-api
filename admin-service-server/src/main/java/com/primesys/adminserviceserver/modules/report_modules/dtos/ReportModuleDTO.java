package com.primesys.adminserviceserver.modules.report_modules.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ReportModuleDTO {
    String id;
    String moduleName;
    String displayName;
    Integer displayOrder;
    List<ReportSubModuleDTO> subModuleDTOS;
}
