package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("admin_daily_activity")
@CompoundIndex(def = "{'user_id': 1, 'date': 1}", unique = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDailyActivityEntity {

    @MongoId
    private ObjectId id;

    @Field("user_id")
    private String userId;

    @Field("user_name")
    private String userName;

    @Field("role_id")
    private Integer roleId;

    // YYYY-MM-DD string — used for range queries and uniqueness
    @Field("date")
    private String date;

    @Field("first_checkin_at")
    private Long firstCheckinAt;

    @Field("last_heartbeat_at")
    private Long lastHeartbeatAt;

    public String getId() {
        return id != null ? id.toString() : null;
    }
}
