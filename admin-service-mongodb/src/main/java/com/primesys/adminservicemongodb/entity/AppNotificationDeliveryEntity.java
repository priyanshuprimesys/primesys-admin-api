package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.enums.NotificationDeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

/**
 * One document per (notification × user) — the user's in-app inbox entry. Created in bulk by NotificationDispatchJob
 * when it fires. The mobile app fetches UNREAD records on startup; marks them READ on open. Collection:
 * app_notification_delivery
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("app_notification_delivery")
@JsonInclude(JsonInclude.Include.NON_NULL)
@CompoundIndexes({ @CompoundIndex(name = "idx_notification", def = "{'notification_id': 1}"),
        @CompoundIndex(name = "idx_user_status", def = "{'user_id': 1, 'status': 1}"),
        // prevents duplicate inbox entries if the job retries
        @CompoundIndex(name = "idx_uniq_notification_user", def = "{'notification_id': 1, 'user_id': 1}", unique = true) })
public class AppNotificationDeliveryEntity {

    @MongoId
    private ObjectId id;

    /** _id of the parent AppUpdateNotificationEntity */
    @Field("notification_id")
    private String notificationId;

    /** userId from UserAppInstallEntity */
    @Field("user_id")
    private String userId;

    /** UNREAD | READ */
    @Field("status")
    private NotificationDeliveryStatus status;

    /** Epoch millis — when the dispatch job created this inbox entry */
    @Field("dispatched_at")
    private Long dispatchedAt;

    /** Epoch millis — when the user opened / acknowledged the notification */
    @Field("read_at")
    private Long readAt;

    @CreatedDate
    @Field("created_at")
    private Date createdAt;

    public String getId() {
        return id != null ? id.toString() : null;
    }
}
