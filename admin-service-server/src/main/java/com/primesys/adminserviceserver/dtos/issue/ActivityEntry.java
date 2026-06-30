package com.primesys.adminserviceserver.dtos.issue;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityEntry {
    /** STATUS_CHANGE | COMMENT | COMMENT_EDIT | COMMENT_DELETE | TRANSFER | CREATED */
    private String type;
    private Long timestamp;
    private String actor;
    private String description;
    private Map<String, Object> details;
}
