package com.primesys.adminserviceserver.modules.reports.services;

import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DeviceConfigStatusDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DeviceReportConfigDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DivisionDeviceReportVariableDTO;

import java.util.List;

public interface ReportConfigurationService {
    List<DivisionDeviceReportVariableDTO> getDevicesReportConfig(String divisionId, Integer deviceTypeId);

    String updateDeviceAndReportStatus(String divisionId, List<DeviceConfigStatusDTO> devices, Integer deviceTypeId);

    String updateDivisionDeviceAndReportStatus(String divisionId, Boolean reportEnable, Boolean activeStatus);

    String divisionDeviceAndReportActiveStatus(String divisionId, Boolean divisionStatus);
}
