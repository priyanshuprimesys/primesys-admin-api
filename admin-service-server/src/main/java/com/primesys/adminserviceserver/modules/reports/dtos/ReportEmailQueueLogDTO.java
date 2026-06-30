package com.primesys.adminserviceserver.modules.reports.dtos;

import com.primesys.adminservicemongodb.enums.EmailStatus;
import com.primesys.adminservicemongodb.enums.ProcessType;

public record ReportEmailQueueLogDTO(String id, String queueId, String divisionId, Integer deviceTypeId,
        Long reportDate, String processDivisionId, String processDivisionName, ProcessType processType,
        String emailSentTo, EmailStatus status, String triggeredBy, String errorMessage, Long processingStartedAt,
        Long createdAt, Long updatedAt) {
}
