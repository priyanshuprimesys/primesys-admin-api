package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceCommandHistoryEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceCommandHistoryRepository extends MongoRepository<DeviceCommandHistoryEntity, ObjectId> {
    List<DeviceCommandHistoryEntity> findByTimestampBetweenOrderByTimestampDesc(long start, long end);

    List<DeviceCommandHistoryEntity> findByDeviceImeiOrderByTimestampDesc(Long deviceImei);
}
