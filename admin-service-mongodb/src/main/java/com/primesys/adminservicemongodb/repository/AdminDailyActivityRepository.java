package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.AdminDailyActivityEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDailyActivityRepository extends MongoRepository<AdminDailyActivityEntity, String> {
}
