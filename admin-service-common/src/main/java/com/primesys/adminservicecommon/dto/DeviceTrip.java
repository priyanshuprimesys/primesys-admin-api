package com.primesys.adminservicecommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTrip {
    Long startTimeStamp;
    Long endTimeStamp;
    Integer tripCount;
    Double tripStartKm;
    Double tripEndKm;
    String sectionName;
    Long deviceImei;
}