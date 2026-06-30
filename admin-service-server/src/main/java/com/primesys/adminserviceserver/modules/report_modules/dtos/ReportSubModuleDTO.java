package com.primesys.adminserviceserver.modules.report_modules.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportSubModuleDTO {
    String id;
    String moduleName;
    String displayName;
    Integer displayOrder;
    Integer typeId;
}
