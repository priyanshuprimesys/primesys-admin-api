package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DevicePacketEntity;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

public interface CustomDevicePacketRepository {
    List<DevicePacketEntity> findByDeviceImeiAndTimestampBetween(Long imei, Long startTime, Long endTime,
            MongoTemplate selectedMongoTemplate);
}
