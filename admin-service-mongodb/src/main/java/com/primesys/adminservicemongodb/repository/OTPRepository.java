package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.OtpEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OTPRepository extends MongoRepository<OtpEntity, ObjectId> {
    Optional<OtpEntity> findByUserId(String userId); // Change to userId

    @Query("{ 'expiresAt' : { $lt: ?0 } }")
    void deleteByExpiresAtBefore(LocalDateTime expirationTime);
}
