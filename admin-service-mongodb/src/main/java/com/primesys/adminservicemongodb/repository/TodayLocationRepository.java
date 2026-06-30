package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.TodayLocationEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodayLocationRepository extends MongoRepository<TodayLocationEntity, String> {

    TodayLocationEntity findByDeviceImei(Long deviceImei);
}
