package com.primesys.adminserviceserver.service;

import com.primesys.adminservicecommon.dto.report.ReportPermissionDTO;
import com.primesys.adminservicecommon.dto.report.UpdateReportPermissionDTO;

import java.util.List;

public interface ReportPermissionService {
    ReportPermissionDTO getReportPermissionById(String id);

    ReportPermissionDTO patchModulesList(UpdateReportPermissionDTO updateReportPermissionDTO);
}
