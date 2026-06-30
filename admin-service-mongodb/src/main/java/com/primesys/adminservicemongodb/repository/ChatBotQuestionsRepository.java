package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.ChatBotQuestionsEntity;
import com.primesys.adminservicemongodb.entity.RdpsGeometryEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatBotQuestionsRepository extends MongoRepository<ChatBotQuestionsEntity, ObjectId> {

}
