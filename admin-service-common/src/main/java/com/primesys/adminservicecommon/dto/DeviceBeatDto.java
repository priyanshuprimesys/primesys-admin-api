package com.primesys.adminservicecommon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceBeatDto {
    String sectionName;
    Integer studentId;
    String beatId;
    Boolean activeStatus;
    Boolean approvedStatus;
    Long startTime;
    Long endTime;
    Long bStartTime;
    Long bEndTime;
    Double tStartKm;
    Double tEndKm;
    Long deviceImei;
    Integer deviceTypeId;
    Integer shiftType;
    String deviceName;
    Integer deviceNo;
    String divisionId;
    String uploadedBy;
    Boolean isMultipleBeat;
    Integer tripNo;
    String approvedBy;
}
