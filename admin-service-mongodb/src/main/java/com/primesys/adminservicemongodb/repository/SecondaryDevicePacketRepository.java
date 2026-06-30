package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DevicePacketEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecondaryDevicePacketRepository
        extends MongoRepository<DevicePacketEntity, String>, CustomDevicePacketRepository {
    // Do not add MongoTemplate or custom logic here
}
