package com.primesys.adminserviceserver.dtos.issue;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class TicketStatsDto {
    private long total;
    private Map<String, Long> byStatus;
    private Map<String, Long> byPriority;
    private Map<String, Long> byAssignee;
}
