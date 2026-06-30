package com.primesys.adminserviceserver.modules.reports.dtos;

import com.primesys.adminservicemongodb.enums.EmailStatus;

import java.util.List;

public record AdminQueueViewDTO(String queueId, String divisionId, String divisionName, Integer deviceTypeId,
        Long reportDate, EmailStatus status, Boolean sent, Long sentAt, Long processingStartedAt, Integer retryCount,
        String errorMessage, Long errorAt, String triggeredBy, Long createdAt, Long updatedAt,
        List<ReportEmailQueueLogDTO> processLogs) {
}
