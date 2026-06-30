package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.BeatEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripReportSummaryRepository extends MongoRepository<TripReportSummaryRepository, ObjectId> {

}
