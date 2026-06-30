package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.WorkflowStep;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkflowStepRepository extends MongoRepository<WorkflowStep, Integer> {
}
