package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.IssueEntity;
import com.primesys.adminservicemongodb.entity.WMessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueDataRepository extends MongoRepository<IssueEntity, String> {

    List<IssueEntity> findByActiveStatusTrue();

    List<IssueEntity> findByActiveStatusTrueAndIssueStatusNot(String status);

    Optional<IssueEntity> findByNoteId(String noteId);

    List<IssueEntity> findByActiveStatusTrueAndCreatedBy(String userId);

    List<IssueEntity> findByWorkflowExpiryLessThanAndEscalationStatusNot(long now, String escalationStatus);
}
