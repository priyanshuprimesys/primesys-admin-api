package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("issue_tickets")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueTicketEntity {

    @MongoId
    private ObjectId id;

    @Field("ticket_id")
    private String ticketId;

    @Field("content_hash")
    private String contentHash;

    @Field("original_msg_hash")
    private String originalMsgHash;

    @Field("active_status")
    private Boolean activeStatus;

    @Field("affected_devices")
    private List<String> affectedDevices;

    @Field("assignee")
    private String assignee;

    @Field("assignee_name")
    private String assigneeName;

    @Field("attachments")
    private List<String> attachments;

    @Field("category")
    private String category;

    @Field("classified_by")
    private String classifiedBy;

    @Field("comments")
    private List<Comment> comments;

    @Field("context")
    private String context;

    @Field("created_at")
    private String createdAt;

    @Field("created_by")
    private String createdBy;

    @Field("device_imei")
    private String deviceImei;

    @Field("division_id")
    private String divisionId;

    @Field("due_date")
    private Long dueDate;

    @Field("group_name")
    private String groupName;

    @Field("human_label")
    private String humanLabel;

    @Field("is_issue")
    private Boolean isIssue;

    @Field("issue_status")
    private String issueStatus;

    @Field("last_updated")
    private String lastUpdated;

    @Field("message")
    private String message;

    @Field("note_id")
    private String noteId;

    @Field("post_time")
    private Long postTime;

    @Field("priority")
    private String priority;

    @Field("sender")
    private String sender;

    @Field("sender_name")
    private String senderName;

    @Field("source_message_id")
    private String sourceMessageId;

    @Field("status_history")
    private List<StatusChangeLog> statusHistory;

    @Field("suggested_action")
    private String suggestedAction;

    @Field("summary")
    private String summary;

    @Field("tags")
    private List<String> tags;

    // Reporter (who raised the ticket, may differ from createdBy system user)
    @Field("reporter")
    private String reporter;

    @Field("reporter_name")
    private String reporterName;

    // Resolution when closed: FIXED, WONT_FIX, DUPLICATE, INVALID, CANNOT_REPRODUCE
    @Field("resolution")
    private String resolution;

    @Field("resolved_at")
    private Long resolvedAt;

    @Field("closed_at")
    private Long closedAt;

    // Team transfer history — full audit trail
    @Field("transfer_history")
    private List<TransferLog> transferHistory;

    // Watchers (user IDs who subscribed to this ticket)
    @Field("watchers")
    private List<String> watchers;

    @Field("watcher_names")
    private List<String> watcherNames;

    // Comment edit/delete audit
    @Field("comment_audit_log")
    private List<CommentAuditLog> commentAuditLog;

    // Custom fields map for extensibility (like Jira custom fields)
    @Field("custom_fields")
    private Map<String, Object> customFields;

    // Linked ticket IDs (blocks / is-blocked-by / relates-to)
    @Field("linked_tickets")
    private List<String> linkedTickets;

    public String getId() {
        return id != null ? id.toString() : null;
    }
}
