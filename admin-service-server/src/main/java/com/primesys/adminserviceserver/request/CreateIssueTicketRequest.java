package com.primesys.adminserviceserver.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateIssueTicketRequest {

    private String message;
    private String groupName;
    private String senderName;
    private String sender;
    private String deviceImei;
    private String divisionId;
    private String sourceMessageId;
    private String noteId;
    private Long postTime;

    private String category;
    private String context;
    private String priority;
    private String summary;
    private String suggestedAction;
    private String classifiedBy;

    private String assignee;
    private String assigneeName;
    private String createdBy;

    private String reporter;
    private String reporterName;

    private List<String> affectedDevices;
    private List<String> tags;
    private List<String> attachments;
    private List<String> watchers;
    private List<String> linkedTickets;

    private Long dueDate;
}
