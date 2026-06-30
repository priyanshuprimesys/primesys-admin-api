package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment {

    @Builder.Default
    private String id = UUID.randomUUID().toString();

    // Primary text — used for new bot/user comments
    @Field("message")
    private String message;

    // Who posted the comment
    // Note: field name in existing MongoDB documents is "commnet_by" (typo preserved)
    @Field("commnet_by")
    private String commentedBy;

    // Source fields — present on comments auto-created from chat/message pipeline
    @Field("msg_hash")
    private String msgHash;

    @Field("source_message_id")
    private String sourceMessageId;

    @Field("received_at")
    private String receivedAt;

    @Field("appended_at")
    private String appendedAt;

    // Role of commenter: USER / SYSTEM / AGENT
    @Field("role")
    private String role;

    @Field("step_id")
    private Integer stepId;

    @Field("commented_at")
    private Long commentedAt;

    // ─── Factory methods ──────────────────────────────────────────────────────

    public static Comment create(String message, String commentedBy, String role, Integer stepId) {
        Comment c = new Comment();
        c.setId(UUID.randomUUID().toString());
        c.setMessage(message);
        c.setCommentedBy(commentedBy);
        c.setRole(role);
        c.setStepId(stepId);
        c.setCommentedAt(System.currentTimeMillis());
        return c;
    }

    public static Comment create(String message, String commentedBy) {
        return create(message, commentedBy, "USER", null);
    }
}
