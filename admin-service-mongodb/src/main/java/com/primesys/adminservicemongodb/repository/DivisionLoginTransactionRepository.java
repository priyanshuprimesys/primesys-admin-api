package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DivisionLoginTransactionEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionLoginTransactionRepository extends MongoRepository<DivisionLoginTransactionEntity, String> {

    List<DivisionLoginTransactionEntity> findByMasterIdOrderByTransactionAtDesc(ObjectId masterId);
}
