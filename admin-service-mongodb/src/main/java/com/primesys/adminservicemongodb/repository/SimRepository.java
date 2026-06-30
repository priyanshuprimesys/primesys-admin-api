package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.SimEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SimRepository extends MongoRepository<SimEntity, String> {

    Optional<SimEntity> findBySimNo(String simNo);

    boolean existsBySimNo(String simNo);

    List<SimEntity> findBySimProvider(String simProvider);
}
