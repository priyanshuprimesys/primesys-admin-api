package com.primesys.adminservicemongodb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceStoppageStatus {
    Long stoppageStartTime;
    Long stoppageEndTime;
    Double stoppageStartKm;
    Double stoppageEndKm;
    String remark;
    Long fenceExitTime;
}
