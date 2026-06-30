package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.ApiAccessLogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiAccessLogRepository extends MongoRepository<ApiAccessLogEntity, String> {

    List<ApiAccessLogEntity> findByUsernameOrderByTimestampDesc(String username);

    List<ApiAccessLogEntity> findByIpAddressOrderByTimestampDesc(String ipAddress);

    List<ApiAccessLogEntity> findByTimestampBetweenOrderByTimestampDesc(long start, long end);
}
