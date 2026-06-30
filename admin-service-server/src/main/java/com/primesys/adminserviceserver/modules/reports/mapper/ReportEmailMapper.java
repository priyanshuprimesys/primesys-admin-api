package com.primesys.adminserviceserver.modules.reports.mapper;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.ReportEmailQueueEntity;
import com.primesys.adminservicemongodb.entity.ReportEmailQueueLogEntity;
import com.primesys.adminserviceserver.modules.reports.dtos.AdminQueueViewDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailQueueLogDTO;

import java.util.List;

public final class ReportEmailMapper {

    public static AdminQueueViewDTO toAdminDTO(ReportEmailQueueEntity queue, String divisionName,
            List<ReportEmailQueueLogDTO> processLogs) {
        return new AdminQueueViewDTO(queue.getId(), queue.getDivisionId(), divisionName, queue.getDeviceTypeId(),
                queue.getReportDate(), queue.getStatus(), queue.getSent(), queue.getSentAt(),
                queue.getProcessingStartedAt(), queue.getRetryCount(), queue.getErrorMessage(), queue.getErrorAt(),
                queue.getTriggeredBy(), queue.getCreatedAt(), queue.getUpdatedAt(), processLogs);
    }

    public static ReportEmailDTO toDTO(ReportEmailQueueEntity reportEmailQueueEntity, DivisionLoginEntity entity,
            List<ReportEmailQueueLogDTO> reportEmailQueueLogDTOS) {
        ReportEmailDTO reportEmailDTO = new ReportEmailDTO();

        reportEmailDTO.setId(reportEmailQueueEntity.getId());
        reportEmailDTO.setReportEndTime(reportEmailQueueEntity.getReportEndTime());
        reportEmailDTO.setReportLockTime(reportEmailQueueEntity.getReportExtendedEndTime());
        reportEmailDTO.setSent(reportEmailQueueEntity.getSent());
        reportEmailDTO.setCreatedAt(reportEmailQueueEntity.getCreatedAt());
        reportEmailDTO.setUpdatedAt(reportEmailQueueEntity.getUpdatedAt());
        reportEmailDTO.setDivisionId(reportEmailQueueEntity.getDivisionId());
        reportEmailDTO.setDivisionName(entity.getName());
        reportEmailDTO.setErrorMessage(reportEmailQueueEntity.getErrorMessage());
        reportEmailDTO.setStatus(reportEmailQueueEntity.getStatus().toString());
        reportEmailDTO.setRetryCount(reportEmailQueueEntity.getRetryCount());
        reportEmailDTO.setTriggeredBy(reportEmailQueueEntity.getTriggeredBy());
        reportEmailDTO.setProcessStartedAt(reportEmailQueueEntity.getProcessingStartedAt());
        reportEmailDTO.setReportEmailLogs(reportEmailQueueLogDTOS);
        if (reportEmailQueueEntity.getSentAt() != null) {
            reportEmailDTO.setSentAt(reportEmailQueueEntity.getSentAt());
        }
        reportEmailDTO.setDeviceTypeId(reportEmailQueueEntity.getDeviceTypeId());
        reportEmailDTO.setReportDate(reportEmailQueueEntity.getReportDate());
        return reportEmailDTO;
    }
}
