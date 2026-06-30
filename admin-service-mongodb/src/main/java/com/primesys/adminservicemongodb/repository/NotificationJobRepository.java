package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.NotificationJobEntity;
import com.primesys.adminservicemongodb.enums.NotificationJobStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationJobRepository extends MongoRepository<NotificationJobEntity, String> {

    /**
     * The query the cron runs every second. Returns jobs that are due and not yet executing.
     */
    List<NotificationJobEntity> findByStatusAndScheduledAtLessThanEqual(NotificationJobStatus status, Long nowMillis);

    List<NotificationJobEntity> findByNotificationId(String notificationId);

    List<NotificationJobEntity> findByStatusOrderByScheduledAtAsc(NotificationJobStatus status);
}
