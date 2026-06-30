package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceExchangeEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceExchangeRepository extends MongoRepository<DeviceExchangeEntity, String> {
    Page<DeviceExchangeEntity> findByUpdatedBy(String exchangeBy, Pageable pageable);

}
