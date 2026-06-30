package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.LocationTransferLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationTransferLogRepository extends MongoRepository<LocationTransferLog, String> {
}
