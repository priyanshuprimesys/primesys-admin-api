package com.primesys.adminserviceserver.modules.division.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DivisionStatsDTO {
    private Integer totalDivision;
    private Integer totalRailwayUser;
    private Integer activeDivision;
    private Integer inactiveDivision;
    private Integer totalDevices;
    private Integer totalActiveDevices;
    private Integer totalInActiveDevices;
}
