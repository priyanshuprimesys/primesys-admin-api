package com.primesys.adminserviceserver.modules.reports.dtos.division_report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TripReportOffTrackStatus {
    long totalOffTrackTime;
    long offTrackStart;
    long offTrackEnd;
}
