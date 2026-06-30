package com.primesys.adminserviceserver.modules.reports.services;

import com.primesys.adminserviceserver.modules.reports.dtos.AdminQueueViewDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailQueueLogDTO;

import java.util.List;

public interface ReportEmailService {
    List<ReportEmailDTO> getAllReportEmailStatus(Long reportDate);

    List<AdminQueueViewDTO> getAdminSystemView(Long reportDate, boolean includeLogs);

    List<ReportEmailQueueLogDTO> getProcessLogsByQueueId(String queueId);

    List<ReportEmailQueueLogDTO> getActiveProcessLogs();

    List<ReportEmailQueueLogDTO> getProcessLogsByDivisionAndDate(String divisionId, Integer deviceTypeId,
            Long reportDate);
}
