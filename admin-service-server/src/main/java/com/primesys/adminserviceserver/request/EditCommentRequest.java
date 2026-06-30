package com.primesys.adminserviceserver.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EditCommentRequest {
    private String issueId;
    private String commentId;
    private String updatedMessage;
    private String updatedBy;
}
