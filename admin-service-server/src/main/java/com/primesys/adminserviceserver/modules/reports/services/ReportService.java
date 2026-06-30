package com.primesys.adminserviceserver.modules.reports.services;

import com.primesys.adminservicemongodb.entity.DivisionReportLogEntity;
import com.primesys.adminserviceserver.modules.reports.dtos.DivisionReportLogDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailResponseDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.TripStatusSummaryDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.TripStatusSummaryReportDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DivisionReportConfigDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ModuleMasterDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ReportConfigDTO;
import org.springframework.expression.spel.ast.Literal;

import java.util.List;

public interface ReportService {
    ReportConfigDTO getConfig(String divisionId);

    DivisionReportConfigDTO getDivisionReportDetail(String divisionId);

    List<DivisionReportLogDTO> getDivisionReportLog(String divisionId, Integer deviceTypeId, Long reportDate);

    String getRegeneratedReportLog(String divisionId, Integer deviceTypeId, Long reportDate);

    String destroyReportLog(String reportId);

    String updateReportLogStatus(String reportId, String divisionId, Integer deviceTypeId, Long reportDate,
            String status);

    List<ModuleMasterDTO> getReportModuleList(String divisionId);

    String updateModuleList(String divisionId, List<String> updatedList);

    List<TripStatusSummaryDTO> getTripReportStatusSummary(String divisionId, Integer deviceType, Long startDate,
            Long endDate);

    String getTripSummaryRegeneratedReport(String divisionId, Integer deviceType, Long reportDate);

    String destroyTripReportSummary(String divisionId, Integer deviceType, Long reportDate);

    List<ReportEmailResponseDTO> getEmailReportLogs(String divisionId, Integer deviceTypeId, Long reportDate);

    String scheduleReportEmail(String divisionId, Integer deviceType, Long reportDate, String userId);
}
