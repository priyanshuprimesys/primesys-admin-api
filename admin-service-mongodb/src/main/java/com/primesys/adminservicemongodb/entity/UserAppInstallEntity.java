package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.enums.AppPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

/**
 * One document per registered user device — the user-base table. The mobile app registers on first launch and updates
 * on every login. The notification dispatch job reads this collection to know whom to notify. Collection:
 * user_app_install
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("user_app_install")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAppInstallEntity {

    @MongoId
    private ObjectId id;

    /**
     * Logical user identifier from the auth system. Indexed — one user can have multiple devices (reinstalls).
     */
    @Indexed
    @Field("division_id")
    private String divisionId;

    /** ANDROID | IOS */
    @Field("platform")
    private AppPlatform platform;

    /**
     * Semantic version currently installed, e.g. "2.0.3". Updated on every app login so force-update checks stay
     * current.
     */
    @Field("app_version")
    private String appVersion;

    /** Hardware identifier sent by the device (IMEI or UUID) */
    @Indexed(unique = true)
    @Field("device_id")
    private String deviceId;

    /** Firebase Cloud Messaging token — refreshed by the app and updated on every login */
    @Field("fcm_token")
    private String fcmToken;
    /**
     * OS version string reported by the device, e.g. "Android 14", "iOS 17.2". Used to identify incompatible versions
     * and target force-update notifications.
     */
    @Field("device_version")
    private String deviceVersion;

    /**
     * Set to false when the user uninstalls or the device is decommissioned. Inactive users are excluded from
     * notification dispatch.
     */
    @Field("is_active")
    private Boolean isActive;

    /**
     * Set to false when the user opts out of in-app notifications in settings. Still counted in the install base but
     * skipped during dispatch.
     */
    @Field("notifications_enabled")
    private Boolean notificationsEnabled;

    /** Epoch millis — last successful authenticated request from this device */
    @Field("last_seen_at")
    private Long lastSeenAt;

    @Field("registered_at")
    private Long registeredAt;

    @Field("updated_at")
    private Long updatedAt;

    public String getId() {
        return id != null ? id.toString() : null;
    }
}
