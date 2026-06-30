package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.model.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("whatsapp_msg")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder

public class WMessageEntity {
    @MongoId
    ObjectId id;
    @Field("sender")
    String sender;
    @Field("group_name")
    String groupName;
    @Field("sender_name")
    String senderName;
    @Field("message")
    String message;
    @Field("division_id")
    String divisionId;
    @Field("note_id")
    String noteId;
    @Field("post_time")
    long postTime;
    @Field("is_issue")
    Boolean isIssue;
    @Field("action_by")
    String actionBy;
    @Field("active_status")
    Boolean activeStatus;
    @Field("updated_by")
    String updatedBy;
    @Field("updated_at")
    Long updatedAt;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }

}
