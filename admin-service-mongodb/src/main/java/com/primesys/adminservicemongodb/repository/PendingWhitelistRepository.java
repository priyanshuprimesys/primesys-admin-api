package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.PendingWhitelistEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PendingWhitelistRepository extends MongoRepository<PendingWhitelistEntity, ObjectId> {

    List<PendingWhitelistEntity> findByOrderByCreatedAtDesc();

    List<PendingWhitelistEntity> findByStatusOrderByCreatedAtDesc(String status);

    List<PendingWhitelistEntity> findByDeviceImeiAndCommandType(Long deviceImei, String commandType);

    List<PendingWhitelistEntity> findByDeviceImei(Long deviceImei);
}
