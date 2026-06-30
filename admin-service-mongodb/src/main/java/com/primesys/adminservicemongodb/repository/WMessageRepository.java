package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.RdpsGeometryEntity;
import com.primesys.adminservicemongodb.entity.WMessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface WMessageRepository extends MongoRepository<WMessageEntity, String> {

    List<WMessageEntity> findByActiveStatusTrue();

    List<WMessageEntity> findByActiveStatusTrueAndPostTimeGreaterThan(long time);

    Optional<WMessageEntity> findByNoteId(String noteId);

    List<WMessageEntity> findAllByNoteId(String noteId);

    List<WMessageEntity> findByNoteIdIn(List<String> inputNoteIds);

    List<WMessageEntity> findByActiveStatusTrueAndIsIssueFalse();

    List<WMessageEntity> findByActiveStatusTrueAndIsIssueFalseAndPostTimeGreaterThan(long time);
}
