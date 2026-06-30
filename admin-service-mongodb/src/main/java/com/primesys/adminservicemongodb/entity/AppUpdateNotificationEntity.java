package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.enums.AppNotificationStatus;
import com.primesys.adminservicemongodb.enums.AppNotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

/**
 * Pure content document — what the notification says. Scheduling and targeting live in NotificationJobEntity.
 * Collection: app_update_notification
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("app_update_notification")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppUpdateNotificationEntity {

    @MongoId
    private ObjectId id;

    @Field("title")
    private String title;

    @Field("message")
    private String message;

    /** UPDATE | FEATURE | GENERAL | MAINTENANCE */
    @Field("notification_type")
    private AppNotificationType notificationType;

    /** Version string of the release, e.g. "2.1.0" — relevant for UPDATE type */
    @Field("app_version")
    private String appVersion;

    /**
     * Devices below this version must update before continuing. Null means the update is optional (soft update).
     */
    @Field("min_required_version")
    private String minRequiredVersion;

    /** If true the mobile app blocks usage until user updates */
    @Field("is_force_update")
    private Boolean isForceUpdate;

    /** Play Store / App Store URL to open when user taps "Update" */
    @Field("store_url")
    private String storeUrl;

    /** What's-new bullet points shown in the update dialog */
    @Field("feature_list")
    private List<String> featureList;

    /** DRAFT → ACTIVE → EXPIRED */
    @Field("status")
    private AppNotificationStatus status;

    /** Epoch millis — when the delivery job ran and inbox records were created */
    @Field("dispatched_at")
    private Long dispatchedAt;

    /** Epoch millis — after this the notification is hidden in-app */
    @Field("expires_at")
    private Long expiresAt;

    /** Total inbox records created by the job (user base at dispatch time) */
    @Field("total_dispatched")
    private Integer totalDispatched;

    /** Incremented each time a user calls mark-as-read */
    @Field("total_read")
    private Integer totalRead;

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
