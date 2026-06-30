package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.JobOrderExecutionLogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobOrderExecutionLogRepository extends MongoRepository<JobOrderExecutionLogEntity, String> {
}
