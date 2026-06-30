package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.BeatEntity;
import com.primesys.adminservicemongodb.entity.RdpsGeometryEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RdpsGeometryRepository extends MongoRepository<RdpsGeometryEntity, ObjectId> {

    List<RdpsGeometryEntity> findByDivisionIdAndActiveStatusTrue(String divisionId);

}
