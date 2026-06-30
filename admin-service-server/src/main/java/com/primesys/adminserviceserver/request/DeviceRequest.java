package com.primesys.adminserviceserver.request;

import com.primesys.adminservicemongodb.model.DevicePayment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceRequest {
    String id;
    long deviceImei;
    String divisionId;
    String deviceName;
    int deviceNo;
    List<String> sosNumbers;
    String deviceSimNo;
    String deviceSimImeiNo;
    String deviceSimImsiNo;
    boolean showGoogleAddress;
    boolean reportAsIndependentRdps;
    int deviceTypeId;
    int reportTimeMargin;
    int onTrackMargin;
    int reportDistMargin;
    long activationDate;
    long paymentDate;
    DevicePayment devicePayment;
    String deviceUserType;
    boolean tripWiseReport;
    String simServiceProvider;
    String deviceVersion;
    String updatedBy;
    long updatedAt;
    int shiftType;
    boolean reportEnable;
    boolean activeStatus;

}
