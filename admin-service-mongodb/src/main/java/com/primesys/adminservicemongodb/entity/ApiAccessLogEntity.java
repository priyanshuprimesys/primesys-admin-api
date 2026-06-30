package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("api_access_log")
public class ApiAccessLogEntity {

    @MongoId
    ObjectId id;

    @Field("ip_address")
    @JsonProperty("ip_address")
    String ipAddress;

    @Field("user_agent")
    @JsonProperty("user_agent")
    String userAgent;

    @Field("method")
    String method;

    @Field("uri")
    String uri;

    @Field("username")
    String username;

    @Field("status_code")
    @JsonProperty("status_code")
    Integer statusCode;

    @Field("response_time_ms")
    @JsonProperty("response_time_ms")
    Long responseTimeMs;

    @Field("timestamp")
    Long timestamp;

    // TTL index: auto-delete records after 30 days
    @Indexed(expireAfterSeconds = 2592000)
    @Field("created_at")
    @JsonProperty("created_at")
    Date createdAt;
}
