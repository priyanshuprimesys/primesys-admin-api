package com.primesys.adminservicemongodb.entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkflowCondition {
    private String field; // e.g. division, imei
    private String operator; // "EQ" (equals) or "CONTAINS"
    private String value; // compare value
    private Integer nextStep; // next step if the condition matches
}
