package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceInfoMaster;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceInfoMasterRepository extends MongoRepository<DeviceInfoMaster, String> {
    DeviceInfoMaster findByDeviceImei(Long deviceImei);
}
