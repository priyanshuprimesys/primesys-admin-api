package com.primesys.adminserviceserver.modules.reports.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TripStatusSummaryReportDTO {
    private String name;
    private String path;

    private String deviceOff;

    private String tripCompleted;

    private String tripNotCompleted;

    private String overSpeed;
    private String delayedStart;
    private String trackDivisionId;

    private long reportOfTheDay;

    private int deviceTypeId;

    private int shiftType;
}
