package com.primesys.adminservicecommon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceDto {
    String deviceId;
    String studentId;
    Integer validDay;
    String name;
    Long imeiNo;
    Boolean showGoogleAddress;
    String simNo;
    Integer deviceTypeId;
    String deviceUsertype;
    Boolean isDeviceConnected;
    Integer deviceNo;
    List<String> sosNumbers;
    String simServiceProvider;
    String deviceSimImsiNo;
    String deviceVersion;
    String divisionId;
    String divisionName;

}
