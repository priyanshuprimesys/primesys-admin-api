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
public class TripReportStatusDto {
    String id;
    Double tripStartKm;
    Double tripEndKm;
    Long tripStartTime;
    Long tripEndTime;
    Integer tripMaxSpeed;
    Integer tripAvgSpeed;
    Double tripActualStartKm;
    Double tripActualEndKm;
    Long tripActualStartTime;
    Long tripActualEndTime;
    Double distanceCoverTrip;
    Double tripDistanceTobeCoverKm;
    Double distanceCoverPoint;
    Long reportOfTheDay;
    Long deviceStartTime;
    Long deviceOffTime;
    Long deviceOnTrackStartTime;
    Long deviceOffTrackStartTime;
    Long deviceImei;
    String deviceName;
    Integer deviceNo;

    Integer tripMaxDuration;
    Long tripMaxTimestamp;

    Integer deviceLocationCount;
    Integer deviceLocationOnTrackCount;
    Integer deviceTripStartBatteryStatus;
    Integer deviceTripEndBatteryStatus;
    String remark;
    List<DeviceSos> deviceSosList;
    List<DeviceStoppage> stoppageList;
    List<TripStatus> status;
    Integer tripNo;
    Integer deviceTypeId;
    Integer shiftType;
    Long createdAt;
    Double tripCount;
    String divisionId;
    String activity;
    Double walkingDistance;
    List<TripReportOffTrackStatus> tripReportOffTrackStatuses;
    List<DeviceStoppageStatusDTO> stoppageStatuses;
}
