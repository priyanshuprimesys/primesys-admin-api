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
public class TripStatusSummaryDTO {
    String deviceName;
    Integer deviceNo;
    Long deviceImei;
    Integer deviceType;
    Long reportOfDay;
    Boolean inActiveDevice;
    private Boolean deviceOff;
    private Boolean tripCompleted;
    private Boolean tripNotCompleted;
    private Boolean overSpeed;
    private Boolean delayedStart;
}
