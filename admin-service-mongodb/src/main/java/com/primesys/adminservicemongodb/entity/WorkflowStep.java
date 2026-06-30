package com.primesys.adminservicemongodb.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("workflow_steps")
public class WorkflowStep {
    @Id
    private Integer id;

    private String message; // may contain placeholders like {division}
    private Integer defaultNextStep; // fallback if no condition matches
    private List<WorkflowCondition> conditions; // branching rules
    private List<String> placeholders; // optional: list of placeholder names
}
