package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceLocation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DeviceLocationRepository extends MongoRepository<DeviceLocation, String> {

    List<DeviceLocation> findByDeviceImeiAndTimestampBetween(Long imei, Long startTimestamp, Long endTimestamp);

    List<DeviceLocation> findByDeviceImeiAndTimestampIn(Long imei, List<Long> timestamps);

    long deleteByDeviceImeiAndTimestampIn(Long imei, List<Long> timestamps);
}
