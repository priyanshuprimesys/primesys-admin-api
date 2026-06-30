package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.JobOrderEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobOrderRepository extends MongoRepository<JobOrderEntity, String> {
}
