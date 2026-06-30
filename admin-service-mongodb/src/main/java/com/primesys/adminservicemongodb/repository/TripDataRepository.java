package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.TripEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripDataRepository extends MongoRepository<TripEntity, ObjectId> {
    TripEntity findByStudentId(Integer studentId);

    List<TripEntity> findByDeviceImeiAndApprovedStatusTrue(Long deviceImei);

    List<TripEntity> findByDeviceImeiAndApprovedStatusTrueAndActiveStatusTrue(Long deviceImei);

    @Query(value = "{ 'device_imei': { $in: ?0 }, 'approved_status': true, 'active_status': true }", fields = "{ 'device_imei': 1, 'start_time': 1, 'end_time': 1 }")
    List<TripTimeProjection> findActiveApprovedTripTimesByDeviceImeis(List<Long> deviceImeis);

    @Query(value = "{ 'device_imei': ?0, 'approved_status': true, 'active_status': true, 'trip_no': 1 }")
    TripEntity findFirstApprovedActiveTrip(Long deviceImei);

    @Query(value = "{ 'device_imei': ?0, 'approved_status': true, 'active_status': true }")
    List<TripEntity> findAllApprovedActiveTrips(Long deviceImei);
}
