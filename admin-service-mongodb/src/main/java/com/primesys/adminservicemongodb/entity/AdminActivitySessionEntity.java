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
@Document("admin_activity_session")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminActivitySessionEntity {

    @MongoId
    private ObjectId id;

    @Field("session_id")
    private String sessionId;

    @Field("user_id")
    private String userId;

    @Field("user_name")
    private String userName;

    @Field("role_id")
    private Integer roleId;

    @Field("page")
    private String page;

    @Field("checkin_at")
    private Long checkedInAt;

    @Field("last_heartbeat")
    private Long lastHeartbeat;

    @Field("checkout_at")
    private Long checkoutAt;

    @Field("active")
    private Boolean active;

    public String getId() {
        return id != null ? id.toString() : null;
    }
}
