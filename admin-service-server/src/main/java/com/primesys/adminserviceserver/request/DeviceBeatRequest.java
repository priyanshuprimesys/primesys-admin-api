package com.primesys.adminserviceserver.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.primesys.adminservicemongodb.model.DevicePayment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class DeviceBeatRequest {
    Long deviceImei;
    String divisionId;
    String deviceName;
    int deviceNo;
    String updatedBy;
    Long updatedAt;
    String sectionName;
    String beatId;
    Boolean activeStatus;
    String startTime;
    String endTime;
    private String bstartTime;
    private String bendTime;
    Double tstartKm;
    Double tendKm;
    String deviceTypeId;
    Boolean isMultipleBeatPath;
    int tripNo;
    Boolean sendAutoPeriodCommand;
    int shiftType;
    Boolean approvedStatus;
    List<ManualBeatTrip> trips;

}
