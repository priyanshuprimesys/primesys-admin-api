package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.AdminActivitySessionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminActivitySessionRepository extends MongoRepository<AdminActivitySessionEntity, String> {

    List<AdminActivitySessionEntity> findByActiveTrue();

    Optional<AdminActivitySessionEntity> findBySessionIdAndActiveTrue(String sessionId);

    List<AdminActivitySessionEntity> findByUserIdAndActiveTrue(String userId);
}
