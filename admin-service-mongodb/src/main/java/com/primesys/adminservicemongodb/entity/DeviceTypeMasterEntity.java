package com.primesys.adminservicemongodb.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "device_type_master")
public class DeviceTypeMasterEntity {

    @MongoId
    ObjectId id;

    @Field("device_type")
    private String deviceType;

    @Field("device_type_id")
    private Integer deviceTypeId;

    @Field("device_name_start_with")
    private String deviceNameStartWith;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
