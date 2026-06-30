package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceHistoryEntity;
import com.primesys.adminservicemongodb.entity.LocationBackupEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface LocationBackupRepository extends MongoRepository<LocationBackupEntity, String> {

    @Query(value = """
                {
                  'device_imei': ?0,
                  'timestamp': { $gte: ?1, $lte: ?2 }
                }
            """)
    List<LocationBackupEntity> findByDeviceImeiAndTimestampRange(Long deviceImei, Long startTime, Long endTime);

    @Query(value = """
            {
              'device_imei': { $in: ?0 },
              'timestamp': { $gte: ?1, $lte: ?2 }
            }
            """)
    List<LocationBackupEntity> findAllByDeviceImeisAndTimestampRange(Collection<Long> deviceImeis, Long startTime,
            Long endTime);
}
