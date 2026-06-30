package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("pending_whitelist")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingWhitelistEntity {

    @MongoId
    ObjectId id;

    @JsonProperty("device_imei")
    @Field("device_imei")
    Long deviceImei;

    @JsonProperty("device_name")
    @Field("device_name")
    String deviceName;

    @JsonProperty("command_type")
    @Field("command_type")
    String commandType;

    @JsonProperty("command")
    @Field("command")
    String command;

    @JsonProperty("sim_provider")
    @Field("sim_provider")
    String simProvider;

    @JsonProperty("parent_id")
    @Field("parent_id")
    Long parentId;

    @JsonProperty("login_name")
    @Field("login_name")
    String loginName;

    @JsonProperty("status")
    @Field("status")
    String status;

    @JsonProperty("created_at")
    @Field("created_at")
    Long createdAt;

    @JsonProperty("updated_at")
    @Field("updated_at")
    Long updatedAt;

    @JsonProperty("updated_by")
    @Field("updated_by")
    String updatedBy;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
