package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.model.BeatGroupByFileDTO;
import com.primesys.adminservicemongodb.entity.BeatEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeatRepository extends MongoRepository<BeatEntity, ObjectId>, BeatRepositoryCustom {
    BeatEntity findByStudentId(Integer studentId);

    List<BeatEntity> findByDeviceImei(Long deviceImei);

    Optional<List<BeatEntity>> findByDeviceImeiAndActiveStatus(Long deviceImei, Boolean activeStatus);

    Optional<List<BeatEntity>> findByDeviceImeiInAndActiveStatus(List<Long> deviceImei, boolean activeStatus);

    List<BeatGroupByFileDTO> findUnapprovedGroupedByRefFileName();

    Optional<List<BeatEntity>> findByRefFileName(String filename);

    void deleteByRefFileName(String refFileName);
}
