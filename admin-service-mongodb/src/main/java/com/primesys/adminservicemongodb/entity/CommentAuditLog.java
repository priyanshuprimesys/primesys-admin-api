package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentAuditLog {
    private String commentId;
    private String action; // "EDIT" or "DELETE"
    private String performedBy;
    private Long performedAt;
    private String oldMessage;
    private String newMessage; // for edits only
}
