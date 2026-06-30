package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("command_master")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceCommandEntity {

    @MongoId
    ObjectId id;

    @Field("Id")
    Integer commandId;

    @Field("Title")
    String title;

    @Field("Command")
    String command;

    @Field("Reply")
    String reply;

    @Field("Description")
    String description;

    @Field("isCustom")
    boolean isCustom;

    @Field("ActiveStatus")
    boolean activeStatus;

    @Field("Priority")
    Integer priority;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
