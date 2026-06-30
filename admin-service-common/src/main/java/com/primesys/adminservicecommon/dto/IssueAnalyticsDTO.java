package com.primesys.adminservicecommon.dto;

import com.primesys.adminservicemongodb.entity.IssueEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IssueAnalyticsDTO {
    private Map<String, Map<String, Long>> assigneeStatusCounts;
    private Map<String, Long> todayStatusCounts;
    private List<PriorityStatusCountDTO> priorityStatusCounts;
    private long reopenedIssuesCount;
    private long slaBreachedCount;
    private List<TagUsageDTO> popularTags;
    // private List<IssueEntity> latestIssues;
    private long overdueIssueCount;
    private double avgTimeToCloseSeconds;
    // ✅ Add these new fields
    private Map<String, Long> statusCountsGlobal;
    private Map<String, Long> todayStatusCountsGlobal;
    private List<PriorityStatusCountDTO> priorityStatusCountsGlobal;
    private long slaBreachedCountGlobal;
    // format: {"2025-07-28": {"OPEN": 5, "CLOSED": 2}}
    private Map<String, Map<String, Long>> statusTrendPerDay;
    private Map<String, Map<String, Long>> statusTrendPerWeek;
    private Map<String, Map<String, Long>> statusTrendPerMonth;
    private String name;
    private Map<String, Long> divisionWiseCounts;
    private PagedResult<IssueEntity> pagedLatestIssues;

}
