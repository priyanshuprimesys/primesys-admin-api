package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.GpsTrackerReport;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GpsTrackerReportRepository extends MongoRepository<GpsTrackerReport, String> {
    Optional<List<GpsTrackerReport>> findByDivisionId(String divisionId);
}
