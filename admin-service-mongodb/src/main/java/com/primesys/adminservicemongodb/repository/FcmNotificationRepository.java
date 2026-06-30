package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.FcmNotificationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FcmNotificationRepository extends MongoRepository<FcmNotificationEntity, String> {
    @Query("{ 'device_imei': { '$in': ?0 } }")
    List<FcmNotificationEntity> findByDeviceImeiIn(List<Long> imeiList);
}
