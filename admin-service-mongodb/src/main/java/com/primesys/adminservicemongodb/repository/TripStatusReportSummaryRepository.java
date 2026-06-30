package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.TripStatusReportSummaryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripStatusReportSummaryRepository extends MongoRepository<TripStatusReportSummaryEntity, String> {

    @Query("{ 'path': { $regex: ?0 }, 'report_of_the_day': { $gte: ?1, $lte: ?2 }, 'device_type_id': ?3 }")
    List<TripStatusReportSummaryEntity> findByPathRegexAndReportOfTheDayBetweenAndDeviceType(String regexDivisionId,
            Long startDateTime, Long endDateTime, int deviceType);

    @Query("{ 'path': { $regex: ?0 }, 'device_type_id': ?1, 'report_of_the_day': ?2 }")
    TripStatusReportSummaryEntity findByPathRegexAndDeviceTypeIdAndReportOfTheDay(String regexPath, int deviceTypeId,
            long reportOfTheDay);

    @Query("{ 'path': { $regex: ?0 }, 'device_type_id': ?1, 'report_of_the_day': ?2 }")
    Boolean existsByPathRegexAndDeviceTypeIdAndReportOfTheDay(String regexPath, int deviceTypeId, long reportOfTheDay);

}
