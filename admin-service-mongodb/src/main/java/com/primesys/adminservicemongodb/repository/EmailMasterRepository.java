package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.EmailMasterEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailMasterRepository extends MongoRepository<EmailMasterEntity, String> {
}
