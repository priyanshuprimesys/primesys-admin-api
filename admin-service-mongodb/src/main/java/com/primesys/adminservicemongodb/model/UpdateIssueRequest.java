package com.primesys.adminservicemongodb.model;

import com.primesys.adminservicemongodb.entity.Comment;
import com.primesys.adminservicemongodb.entity.IssueEntity;
import lombok.Data;

import java.util.List;

@Data
public class UpdateIssueRequest {
    private String issueId;
    private IssueEntity issue;
    private String updatedBy;
}
