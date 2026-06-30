package com.primesys.adminserviceserver.modules.reports.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportEmailDTO {
    String id;
    String divisionId;
    String divisionName;
    Integer deviceTypeId;
    Long reportDate;
    Long reportEndTime;
    Long reportLockTime;
    String status;
    Long processStartedAt;
    Integer retryCount;
    String errorMessage;
    String triggeredBy;
    Long createdAt;
    Long updatedAt;
    Boolean sent;
    Long sentAt;
    List<ReportEmailQueueLogDTO> reportEmailLogs;
}
