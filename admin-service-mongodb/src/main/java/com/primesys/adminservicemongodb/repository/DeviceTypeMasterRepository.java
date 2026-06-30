package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceTypeMasterEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceTypeMasterRepository extends MongoRepository<DeviceTypeMasterEntity, String> {
}
