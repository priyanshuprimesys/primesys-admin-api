package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DevicePacketEntity;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SecondaryDevicePacketRepositoryImpl implements CustomDevicePacketRepository {

    private final MongoTemplate mongoTemplate;

    public SecondaryDevicePacketRepositoryImpl(MongoTemplate secondaryMongoTemplate) {
        this.mongoTemplate = secondaryMongoTemplate;
    }

    public List<DevicePacketEntity> findByDeviceImeiAndTimestampBetween(Long imei, Long startTime, Long endTime,
            MongoTemplate selectedMongoTemplate) {
        Query query = new Query(Criteria.where("deviceImei").is(imei).and("timestamp").gte(startTime).lte(endTime));
        return selectedMongoTemplate.find(query, DevicePacketEntity.class);
    }
}
