package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Document("command_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceCommandHistoryEntity {
    @MongoId
    ObjectId id;
    @JsonProperty("device_imei")
    @Field("device_imei")
    Long deviceImei;
    @JsonProperty("command")
    @Field("command")
    String command;
    @Field("device")
    private Long device;
    // @Field("Id")
    // Integer commandId;
    @JsonProperty("timestamp")
    @Field("timestamp")
    long timestamp;

    @JsonProperty("device_name")
    @Field("name")
    String deviceName;

    @JsonProperty("delivered_msg")
    @Field("delivered_msg")
    String deliveredMessage;
    @JsonProperty("login_name")

    @Field("login_name")
    String loginName;
    @JsonProperty("device_response")

    @Field("device_response")
    String deviceResponse;
    @JsonProperty("division_id")

    @Field("division_id")
    String divisionId;
    @JsonProperty("device_response_time")

    @Field("device_response_time")
    long deviceResponseTime;
    @JsonProperty("is_active")

    @Field("is_active")
    boolean isActive;
    @Field("pid")
    private String pid;
    @Field("original_command")
    private String originalCommand;
    @Field("parent_id")
    private String parentId;
    @Field("ttl_timestamp")
    private Date ttlTimestamp;
    @Field("is_resend")
    private boolean isResend;
    @Field("resent_at")
    private long resentAt;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
