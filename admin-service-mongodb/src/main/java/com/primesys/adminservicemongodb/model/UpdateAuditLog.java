package com.primesys.adminservicemongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAuditLog {
    private String field; // e.g., "priority"
    private String oldValue;
    private String newValue;
    private String updatedBy;
    private Long updatedAt;
}
