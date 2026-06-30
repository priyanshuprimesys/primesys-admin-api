package com.primesys.adminservicemongodb.model;

public interface DeviceHistory {
    Long getDeviceImei();

    Long getTimestamp();

    Boolean getBlind();

    GeoLocation getGeoLocation();

    Integer getSpeed();

    Object getVoltageLevel();

    Object getGsmSignalStrength();

    Object getNearestRdps();
}
