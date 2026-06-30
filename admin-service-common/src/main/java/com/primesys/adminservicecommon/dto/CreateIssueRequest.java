package com.primesys.adminservicecommon.dto;

import com.primesys.adminservicemongodb.entity.Comment;
import lombok.Data;
import java.util.List;

@Data
public class CreateIssueRequest {
    private String wMsgId;
    private String sender;
    private String groupName;
    private String senderName;
    private String message;
    private String noteId;
    private long postTime;
    private Boolean isIssue;
    private String issueStatus;
    private String priority;
    private String category;
    private String assignee;
    private List<Comment> comments;
    private List<String> tags;
    private List<String> attachments;
    private Long dueDate;
    private String createdBy;
    private String divisionId;
    private String deviceImei;
}
