package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.enums.NotificationJobStatus;
import com.primesys.adminservicemongodb.enums.NotificationTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.List;

/**
 * Scheduled execution unit for a notification broadcast. The cron (NotificationDispatchJob) queries this collection
 * every second for records where status=PENDING AND scheduled_at <= now(). Collection: notification_jobs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("notification_jobs")
@JsonInclude(JsonInclude.Include.NON_NULL)
@CompoundIndexes({
        // the query the cron runs every second — must be fast
        @CompoundIndex(name = "idx_dispatch_sweep", def = "{'status': 1, 'scheduled_at': 1}") })
public class NotificationJobEntity {

    @MongoId
    private ObjectId id;

    /** _id of the AppUpdateNotificationEntity this job will dispatch */
    @Field("notification_id")
    private String notificationId;

    /** PENDING → RUNNING → COMPLETED | FAILED */
    @Field("status")
    private NotificationJobStatus status;

    /**
     * Epoch millis — the earliest time the cron is allowed to execute this job. Set to now() for immediate dispatch, or
     * a future timestamp for scheduling.
     */
    @Field("scheduled_at")
    private Long scheduledAt;

    /** Epoch millis — when the cron actually started executing */
    @Field("executed_at")
    private Long executedAt;

    /** Epoch millis — when the job finished (success or failure) */
    @Field("completed_at")
    private Long completedAt;

    /** ALL — every active+opted-in user; SPECIFIC — only targetUserIds */
    @Field("target_type")
    private NotificationTargetType targetType;

    /**
     * Populated only when targetType == SPECIFIC. Contains userIds from UserAppInstallEntity.
     */
    @Field("target_user_ids")
    private List<String> targetUserIds;

    /** Number of AppNotificationDeliveryEntity records created by this job */
    @Field("total_dispatched")
    private Integer totalDispatched;

    /** Filled when status == FAILED */
    @Field("error_message")
    private String errorMessage;

    @Field("created_by")
    private String createdBy;

    @CreatedDate
    @Field("created_at")
    private Date createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private Date updatedAt;

    public String getId() {
        return id != null ? id.toString() : null;
    }
}
