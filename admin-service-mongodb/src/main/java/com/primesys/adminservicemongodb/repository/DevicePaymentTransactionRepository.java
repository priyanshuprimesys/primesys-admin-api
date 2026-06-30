package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.model.DevicePayment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface DevicePaymentTransactionRepository extends MongoRepository<DevicePayment, String> {

}
