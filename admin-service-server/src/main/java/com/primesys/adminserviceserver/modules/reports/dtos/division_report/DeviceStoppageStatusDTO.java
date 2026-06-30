package com.primesys.adminserviceserver.modules.reports.dtos.division_report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceStoppageStatusDTO {
    Long stoppageStartTime;
    Long stoppageEndTime;
    Double stoppageStartKm;
    Double stoppageEndKm;
    String remark;
    Long fenceExitTime;
}
