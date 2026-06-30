package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.LocationOutOfIndia;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationOutOfIndiaRepository extends MongoRepository<LocationOutOfIndia, String> {
}
