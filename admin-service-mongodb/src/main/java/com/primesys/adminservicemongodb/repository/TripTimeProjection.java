package com.primesys.adminservicemongodb.repository;

public interface TripTimeProjection {

    Long getDeviceImei();

    Long getStartTime();

    Long getEndTime();
}