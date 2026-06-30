package com.primesys.adminserviceserver.modules.reports.dtos.division_report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripStatus {
    private String inOutStatus;
    private Long timestamp;
    private String allocatedLc;
    private String location;
    private Integer distance;
    private Integer timeDeviation;
}