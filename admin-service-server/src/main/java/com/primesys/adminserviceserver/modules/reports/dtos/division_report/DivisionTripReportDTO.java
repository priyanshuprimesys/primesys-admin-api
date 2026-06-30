package com.primesys.adminserviceserver.modules.reports.dtos.division_report;

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
public class DivisionTripReportDTO {
    Double tripStartKm;
    Double tripEndKm;
    Long tripStartTime;
    Long tripEndTime;
    Integer tripMaxSpeed;
    Integer tripAvgSpeed;
    Double distanceCoverTrip;
    Double tripDistanceTobeCoverKm;
    Long reportOfTheDay;
    Double tripActualStartKm;
    Double tripActualEndKm;
    Long actualStartTime;
    Long actualEndTime;
    Long deviceImei;
    String deviceName;
    Integer allocatedTrips;
    Integer actualTrips;
    Integer deviceNo;
    String sectionName;
    Integer deviceTripStartBatteryStatus;
    Integer deviceTripEndBatteryStatus;
    Boolean isBlind;

    String remark;
    List<TripReportStatusDto> tripList;
    Integer deviceTypeId;
    Integer shiftType;
    Integer tripMaxDuration;
    Long tripMaxTimestamp;
    private List<TripStatus> status;
    Long createdAt;
    Double walkingDistance;

}