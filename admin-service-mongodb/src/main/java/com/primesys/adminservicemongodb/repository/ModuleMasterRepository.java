package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleMasterRepository extends MongoRepository<ModuleMasterEntity, String> {

    // @Query("{'$and':[{'division_id':?0},{'device_no':{'$in':?1}}]}")
    // List<DeviceEntity> findByNoTrackDivId(String objectId, List<Integer> ints);

}
