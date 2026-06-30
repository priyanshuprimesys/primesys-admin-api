package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceHistoryEntity;
import com.primesys.adminservicemongodb.entity.DeviceLocation;
import com.primesys.adminservicemongodb.model.DeviceHistory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DeviceHistoryRepository extends MongoRepository<DeviceHistoryEntity, String> {

    @Query(value = """
            {
              'device_imei': ?0,
              'timestamp': { $gte: ?1, $lte: ?2 }
            }
            """, fields = """
            {
              '_id': 0,
              'device_imei': 1,
              'timestamp': 1,
              'geo_location': 1,
              'blind':1,
              'speed': 1,
              'voltage_level': 1,
              'nearest_rdps.kilometer': 1,
              'nearest_rdps.distance': 1,
              'nearest_rdps.distance_diff': 1
            }
            """)
    List<DeviceHistory> findLightHistoryForTrip(Long deviceImei, Long startTs, Long endTs, Sort sort);

    List<DeviceHistoryEntity> findByDeviceImeiAndTimestampBetween(Long device, Long startTime, Long endTime);

    @Query(value = """
            {
              'device_imei': { $in: ?0 },
              'timestamp': { $gte: ?1, $lte: ?2 }
            }
            """)
    List<DeviceHistoryEntity> findAllByDeviceImeisAndTimestampRange(Collection<Long> deviceImeis, Long startTime,
            Long endTime);
}
