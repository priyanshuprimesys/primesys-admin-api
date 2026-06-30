package com.primesys.adminservicemongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DivisionTripReport {
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
    List<TripReportStatus> tripList;
    Integer deviceTypeId;
    Integer shiftType;
    Integer tripMaxDuration;
    Long tripMaxTimestamp;
    private List<TripStatus> status;
    Long createdAt;
    Double walkingDistance;

}