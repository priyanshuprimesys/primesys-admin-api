package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.AppUpdateNotificationEntity;
import com.primesys.adminservicemongodb.enums.AppNotificationStatus;
import com.primesys.adminservicemongodb.enums.AppNotificationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppUpdateNotificationRepository extends MongoRepository<AppUpdateNotificationEntity, String> {

    List<AppUpdateNotificationEntity> findByStatusOrderByCreatedAtDesc(AppNotificationStatus status);

    List<AppUpdateNotificationEntity> findByNotificationTypeAndStatusOrderByCreatedAtDesc(AppNotificationType type,
            AppNotificationStatus status);

    /** Used to check if an active UPDATE notification already exists for a given version */
    boolean existsByAppVersionAndStatus(String appVersion, AppNotificationStatus status);
}
