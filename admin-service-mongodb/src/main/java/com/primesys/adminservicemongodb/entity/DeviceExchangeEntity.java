package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.model.DevicePayment;
import com.primesys.adminservicemongodb.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.List;

@Document("devices_exchange")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceExchangeEntity {

    @MongoId
    ObjectId id;
    @Field("old_device")
    DeviceEntity oldDevice;
    @Field("new_device")
    DeviceEntity newDevice;
    @Field("timestamp")
    Long timestamp;
    @Field("updated_by")
    String updatedBy;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
