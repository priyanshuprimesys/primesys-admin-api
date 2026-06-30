package com.primesys.adminserviceserver.modules.reports.dtos;

import com.primesys.adminservicemongodb.enums.StatusEnum;
import com.primesys.adminservicemongodb.model.DivisionTripReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DivisionReportLogDTO {
    private String id;
    private String divisionId;

    private Integer deviceTypeId;

    private Long generatedAt;

    /// the maximum end time a trip has to have means if 10 devices have 05 - 09 and another 4 has 10 hrs - 17 hrs then
    /// 17 is max time
    private Long tripMaxTime;

    /// will save in seconds like 120 will convert to 2 hours
    private Long tripLockTime;

    private Long reportDate;

    private StatusEnum status;

    private List<DivisionTripReport> reports;

}
