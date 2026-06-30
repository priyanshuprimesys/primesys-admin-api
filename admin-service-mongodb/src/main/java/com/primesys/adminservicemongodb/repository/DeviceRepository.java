package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.model.DeviceImeiNoOnly;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends MongoRepository<DeviceEntity, String> {

    DeviceEntity findByDeviceImei(Long imei);

    List<DeviceEntity> findAllByActiveStatusTrue();

    List<DeviceEntity> findByDivisionId(String divisionId);

    List<DeviceEntity> findByDivisionIdAndDeviceTypeId(String divisionId, Integer deviceTypeId);

    // @Query("{'$and':[{'division_id':?0},{'device_no':{'$in':?1}},{'active_status':true}]}")
    @Query("{'$and':[{'division_id':?0},{'device_no':{'$in':?1}}]}")
    List<DeviceEntity> findByNoTrackDivId(String objectId, List<Integer> ints);

    // @Query(value = "{'division_id': ?0 , 'device_no': ?1}")
    // DeviceEntity findByDivisionIdDeviceNo(String divisionId,Integer deviceNo);

    void deleteById(ObjectId id);

    // @Query("{'$and':[{'division_id':?0},{'device_no':?1},{'active_status':true}]}")
    @Query("{'$and':[{'division_id':?0},{'device_no':?1}]}")
    DeviceEntity findByNoTrackDivId(String objectId, Integer deviceNo);

    @Query("{ '_id': { '$in': ?0 } }")
    List<DeviceEntity> findByIdsIn(List<ObjectId> ids);

    DeviceEntity findByDivisionIdAndDeviceNo(String divisionId, Integer deviceNo);

    List<DeviceEntity> findByDivisionIdAndDeviceNoIn(String divisionId, List<Integer> deviceNos);

    @Query(value = "{ 'division_id': ?0, 'active_status': true, 'device_type_id': ?1, 'device_no': { $in: ?2 } }", fields = "{ 'device_no': 1,'device_imei': 1,'shiftType':1, '_id': 0 }")
    List<DeviceImeiNoOnly> findDeviceNosByDivisionAndDeviceType(String divisionId, Integer deviceTypeId,
            List<Integer> deviceNos);

    @Query(value = "{ 'division_id' : ?0, 'device_type_id' : ?1, 'device_no' : { $in : ?2 } }")
    List<DeviceEntity> findDevicesByDivisionAndDeviceType(String divisionId, Integer deviceTypeId,
            List<Integer> deviceNos);

    @Query(value = "{ 'division_id' : ?0, 'device_no' : { $in : ?2 } }")
    List<DeviceEntity> findDevicesByDivisionAndDeviceNos(String divisionId, List<Integer> deviceNos);

    @Query(value = "{ 'division_id' : ?0, 'device_type_id' : ?1, 'device_no' : ?2 }")
    DeviceEntity findDeviceByDivisionAndDeviceType(String divisionId, Integer deviceTypeId, Integer deviceNo);
}
