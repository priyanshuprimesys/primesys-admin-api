package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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

@Document("fcm_notification")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FcmNotificationEntity {

    @MongoId
    ObjectId id;
    @Field("device_imei")
    Long deviceImei;
    @Field("notification_title")
    String notificationTitle;
    @Field("notification_body")
    String notificationBody;
    @Field("day_timestamp")
    Long day_timestamp;
    @Field("timestamp")
    Long timestamp;
    @CreatedDate
    @Field("created_at")
    private Date createdAt;
    private String updatedBy;
    @Field("updated_at")
    private long updatedAt;
    @Field("active_status")
    @JsonProperty("active_status")
    private Boolean activeStatus;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
