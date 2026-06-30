package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("issue_skip_msg")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueSkipMsgEntity {

    @MongoId
    private ObjectId id;

    @Field("note_id")
    private String noteId;

    @Field("message")
    private String message;

    @Field("sender")
    private String sender;

    @Field("sender_name")
    private String senderName;

    @Field("group_name")
    private String groupName;

    @Field("division_id")
    private String divisionId;

    @Field("post_time")
    private Long postTime;

    @Field("source_message_id")
    private String sourceMsgId;

    @Field("category")
    private String category;

    @Field("summary")
    private String summary;

    @Field("is_issue")
    private Boolean isIssue;

    @Field("reviewed")
    private Boolean reviewed;

    @Field("promoted_to_ticket")
    private Boolean promotedToTicket;

    @Field("received_at")
    private String receivedAt;

    @Field("skipped_by")
    private String skippedBy;

    @Field("skipped_at")
    private String skippedAt;

    @Field("converted")
    private Boolean converted;

    @Field("converted_ticket_id")
    private String convertedTicketId;

    @Field("converted_by")
    private String convertedBy;

    @Field("converted_at")
    private Long convertedAt;

    public String getId() {
        return id != null ? id.toString() : null;
    }
}
