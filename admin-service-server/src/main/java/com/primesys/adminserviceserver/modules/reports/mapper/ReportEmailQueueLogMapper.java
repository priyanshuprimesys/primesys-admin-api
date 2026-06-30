package com.primesys.adminserviceserver.modules.reports.mapper;

import com.primesys.adminservicemongodb.entity.ReportEmailQueueLogEntity;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailQueueLogDTO;

public final class ReportEmailQueueLogMapper {

    private ReportEmailQueueLogMapper() {
    }

    public static ReportEmailQueueLogDTO toDTO(ReportEmailQueueLogEntity entity) {
        return new ReportEmailQueueLogDTO(entity.getId(), entity.getQueueId(), entity.getDivisionId(),
                entity.getDeviceTypeId(), entity.getReportDate(), entity.getProcessDivisionId(),
                entity.getProcessDivisionName(), entity.getProcessType(), entity.getEmailSentTo(), entity.getStatus(),
                entity.getTriggeredBy(), entity.getErrorMessage(), entity.getProcessingStartedAt(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
