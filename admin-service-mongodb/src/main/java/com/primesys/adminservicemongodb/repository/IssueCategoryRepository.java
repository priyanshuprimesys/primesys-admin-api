package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.IssueCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IssueCategoryRepository extends MongoRepository<IssueCategory, String> {
}
