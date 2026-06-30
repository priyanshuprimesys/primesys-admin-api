package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.AppNotificationDeliveryEntity;
import com.primesys.adminservicemongodb.enums.NotificationDeliveryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppNotificationDeliveryRepository extends MongoRepository<AppNotificationDeliveryEntity, String> {

    /** All inbox entries for one notification — "who got it" list */
    List<AppNotificationDeliveryEntity> findByNotificationId(String notificationId);

    /**
     * Mobile app calls this on startup to fetch the user's unread inbox. Returns all UNREAD entries so the app can show
     * banners/badges.
     */
    List<AppNotificationDeliveryEntity> findByUserIdAndStatus(String userId, NotificationDeliveryStatus status);

    /** Find the specific inbox entry to mark it READ */
    Optional<AppNotificationDeliveryEntity> findByNotificationIdAndUserId(String notificationId, String userId);

    /** Count UNREAD for a notification — feeds the "not yet seen" dashboard */
    long countByNotificationIdAndStatus(String notificationId, NotificationDeliveryStatus status);

    /** Count READ for a notification — feeds totalRead on the notification */
    long countByNotificationId(String notificationId);
}
