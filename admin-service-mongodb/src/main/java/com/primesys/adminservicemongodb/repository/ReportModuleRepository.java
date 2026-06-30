package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.ReportModule;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReportModuleRepository extends MongoRepository<ReportModule, ObjectId> {

    Optional<ReportModule> findByModuleName(String moduleName);
}
