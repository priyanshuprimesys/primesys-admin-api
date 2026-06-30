package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("issue_transfer_audit")
public class IssueTransferAudit {

    @MongoId
    private ObjectId id;

    @Field("issue_id")
    private String issueId;

    @Field("from_assignee")
    private String fromAssignee;

    @Field("to_assignee")
    private String toAssignee;

    @Field("transferred_by")
    private String transferredBy;

    @Field("transferred_at")
    private Long transferredAt;

    @Field("transfer_reason")
    private String transferReason;
}
