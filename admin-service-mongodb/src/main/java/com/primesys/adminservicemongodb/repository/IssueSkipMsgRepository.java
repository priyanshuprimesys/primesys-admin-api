package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.IssueSkipMsgEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueSkipMsgRepository extends MongoRepository<IssueSkipMsgEntity, String> {

    List<IssueSkipMsgEntity> findByConvertedFalseOrConvertedIsNull();

    List<IssueSkipMsgEntity> findAll();

    Optional<IssueSkipMsgEntity> findByNoteId(String noteId);
}
