package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.IssueTransferAudit;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IssueTransferAuditRepository extends MongoRepository<IssueTransferAudit, String> {

    List<IssueTransferAudit> findByIssueId(String issueId, Sort sort);
}
