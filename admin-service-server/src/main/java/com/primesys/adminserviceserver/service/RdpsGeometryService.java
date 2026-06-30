package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.ChatBotQuestionsEntity;
import com.primesys.adminservicemongodb.entity.RdpsGeometryEntity;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface RdpsGeometryService {

    List<RdpsGeometryEntity> saveRdps(List<RdpsGeometryEntity> rdps);

    Optional<String> uploadRdpsFile(String filePath, String divisionId);

    List<RdpsGeometryEntity> getDivisionRdpsData(String divisionId);

    String deleteRdpsData(String rdpsId);

    // Find an entity by ID
    Optional<RdpsGeometryEntity> findById(String id);

    RdpsGeometryEntity save(RdpsGeometryEntity existingEntity);

}
