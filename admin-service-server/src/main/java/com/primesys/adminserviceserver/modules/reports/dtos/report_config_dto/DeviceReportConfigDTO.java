package com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DeviceReportConfigDTO {
    String deviceName;
    Integer deviceNo;
    Integer deviceType;
    Integer shiftType;
    Boolean activeStatus;
    Boolean reportEnable;
    Integer reportDistMargin;
    Integer reportTimeMargin;
}
