package com.primesys.adminservicecommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueBriefDTO {
    private String id;
    private String assignee;
    private String status;
    private long createdAt;
}
