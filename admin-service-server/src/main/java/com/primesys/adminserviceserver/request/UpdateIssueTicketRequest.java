package com.primesys.adminserviceserver.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.primesys.adminserviceserver.utility.StringOrNumberDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateIssueTicketRequest {

    private String id;

    private String issueStatus;
    private String priority;
    private String category;
    private String context;
    private String summary;
    private String suggestedAction;
    private String humanLabel;

    private String assignee;
    private String assigneeName;

    private String divisionId;
    private Long dueDate;

    // accepts both "359751090247995" (string) and 359751090247995 (number) from JSON
    @JsonDeserialize(using = StringOrNumberDeserializer.class)
    private String deviceImei;

    // Resolution when closing: FIXED, WONT_FIX, DUPLICATE, INVALID, CANNOT_REPRODUCE
    private String resolution;

    private List<String> affectedDevices;
    private List<String> tags;
    private List<String> attachments;
    private List<String> linkedTickets;

    private String updatedBy;
}
