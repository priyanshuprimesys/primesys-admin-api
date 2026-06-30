package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.model.UpdateAuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.lang.constant.Constable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("issue_data")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class IssueEntity {

    @MongoId
    private ObjectId id;

    @Field("w_msg_id")
    private String wMsgId;

    @Field("sender")
    private String sender;

    @Field("group_name")
    private String groupName;

    @Field("sender_name")
    private String senderName;

    @Field("message")
    private String message;

    @Field("note_id")
    private String noteId;

    @Field("post_time")
    private long postTime;

    @Field("is_issue")
    private Boolean isIssue;

    @Field("issue_status")
    private String issueStatus;

    @Field("priority")
    private String priority; // e.g., LOW, MEDIUM, HIGH, CRITICAL

    @Field("category")
    private String category; // e.g., BUG, FEATURE_REQUEST, QUERY

    @Field("assignee")
    private String assignee;

    @Field("assignee_name")
    private String assigneeName;
    @Field("previous_assignee")
    private String previousAssignee;

    @Field("transfer_history")
    private List<TransferLog> transferHistory;

    @Field("comments")
    private List<Comment> comments;

    @Field("tags")
    private List<String> tags;

    @Field("attachments")
    private List<String> attachments; // e.g., file URLs or storage IDs

    @Field("due_date")
    private Long dueDate;

    @Field("reopen_count")
    private Integer reopenCount;

    @Field("status_history")
    private List<StatusChangeLog> statusHistory;

    @Field("active_status")
    private Boolean activeStatus;

    @Field("action_by")
    private String actionBy;

    @Field("updated_by")
    private String updatedBy;

    @Field("updated_at")
    private Long updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("created_at")
    private Long createdAt;

    @Field("division_id")
    private String divisionId;
    @Field("comment_audit_logs")
    private List<CommentAuditLog> commentAuditLogs;
    @Field("update_audit_logs")
    private List<UpdateAuditLog> updateAuditLogs;
    @Field("device_imei")
    private String deviceImei;
    // Workflow-specific fields
    @Field("current_step_id")
    private Integer currentStepId;

    @Field("workflow_expiry")
    private Long workflowExpiry; // epoch millis

    @Field("escalation_status")
    private String escalationStatus;

    public String getId() {
        return id != null ? id.toString() : null;
    }
}
