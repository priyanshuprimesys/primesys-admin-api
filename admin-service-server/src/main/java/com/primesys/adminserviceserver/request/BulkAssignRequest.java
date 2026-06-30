package com.primesys.adminserviceserver.request;

import lombok.Data;

import java.util.List;

@Data
public class BulkAssignRequest {
    private List<String> ids;
    private String assignee;
    private String assigneeName;
    private String assignedBy;
}
