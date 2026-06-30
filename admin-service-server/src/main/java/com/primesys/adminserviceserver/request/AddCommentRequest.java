package com.primesys.adminserviceserver.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddCommentRequest {

    private String issueId;
    private String message;
    private String commentedBy;
    private String role; // USER / SYSTEM / AGENT (defaults to USER)
}
