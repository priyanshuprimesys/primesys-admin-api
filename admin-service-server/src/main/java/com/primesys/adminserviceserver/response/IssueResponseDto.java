package com.primesys.adminserviceserver.response;

import com.primesys.adminservicemongodb.entity.Comment;
import com.primesys.adminservicemongodb.entity.CommentAuditLog;
import com.primesys.adminservicemongodb.entity.StatusChangeLog;
import com.primesys.adminservicemongodb.entity.TransferLog;
import com.primesys.adminservicemongodb.model.UpdateAuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IssueResponseDto {

    private String id;

    private String wMsgId;
    private String sender;
    private String groupName;
    private String senderName;
    private String message;
    private String noteId;
    private Long postTime;

    private Boolean isIssue;
    private String issueStatus;
    private String priority;
    private String category;

    private String assignee;
    private String assigneeName;
    private String previousAssignee;

    private List<TransferLog> transferHistory;
    private List<Comment> comments;
    private List<String> tags;
    private List<String> attachments;

    private Long dueDate;
    private Integer reopenCount;
    private List<StatusChangeLog> statusHistory;

    private Boolean activeStatus;
    private String actionBy;
    private String updatedBy;
    private Long updatedAt;
    private String createdBy;
    private Long createdAt;

    private String divisionId;
    private List<CommentAuditLog> commentAuditLogs;
    private List<UpdateAuditLog> updateAuditLogs;

    private String deviceImei;
}
