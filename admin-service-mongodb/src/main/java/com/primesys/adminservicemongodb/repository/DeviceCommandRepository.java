package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceCommandEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceCommandRepository extends MongoRepository<DeviceCommandEntity, ObjectId> {
}
