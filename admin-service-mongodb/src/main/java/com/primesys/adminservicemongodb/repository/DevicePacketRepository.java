package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DevicePacketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevicePacketRepository extends MongoRepository<DevicePacketEntity, String> {
    List<DevicePacketEntity> findByDeviceImei(Long deviceImei); // Query by device IMEI

    // List<DevicePacketEntity> findByDeviceImeiAndTimestampBetween(Long deviceImei, Long startTime, Long endTime);
    Page<DevicePacketEntity> findByDeviceImeiAndTimestampBetween(Long imei, Long startTime, Long endTime,
            Pageable pageable);
}
