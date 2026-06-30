package com.primesys.adminserviceserver.job;

import com.primesys.adminservicemongodb.entity.AppNotificationDeliveryEntity;
import com.primesys.adminservicemongodb.entity.AppUpdateNotificationEntity;
import com.primesys.adminservicemongodb.entity.NotificationJobEntity;
import com.primesys.adminservicemongodb.entity.UserAppInstallEntity;
import com.primesys.adminservicemongodb.enums.AppNotificationStatus;
import com.primesys.adminservicemongodb.enums.NotificationDeliveryStatus;
import com.primesys.adminservicemongodb.enums.NotificationJobStatus;
import com.primesys.adminservicemongodb.enums.NotificationTargetType;
import com.primesys.adminservicemongodb.repository.AppNotificationDeliveryRepository;
import com.primesys.adminservicemongodb.repository.AppUpdateNotificationRepository;
import com.primesys.adminservicemongodb.repository.NotificationJobRepository;
import com.primesys.adminservicemongodb.repository.UserAppInstallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchJob {

    // private final NotificationJobRepository jobRepo;
    // private final AppUpdateNotificationRepository notificationRepo;
    // private final UserAppInstallRepository userInstallRepo;
    // private final AppNotificationDeliveryRepository deliveryRepo;

    /**
     * Runs every second. Picks up any PENDING notification jobs whose scheduled_at has arrived and creates one inbox
     * record per target user.
     */
    // @Scheduled(fixedDelay = 1000)
    // public void sweep() {
    // long now = System.currentTimeMillis();
    //
    // List<NotificationJobEntity> dueJobs =
    // jobRepo.findByStatusAndScheduledAtLessThanEqual(NotificationJobStatus.PENDING, now);
    //
    // for (NotificationJobEntity job : dueJobs) {
    // dispatch(job, now);
    // }
    // }

    // private void dispatch(NotificationJobEntity job, long now) {
    // // lock the job immediately so a second sweep won't pick it up
    // job.setStatus(NotificationJobStatus.RUNNING);
    // job.setExecutedAt(now);
    // jobRepo.save(job);
    //
    // try {
    // List<UserAppInstallEntity> targets = resolveTargets(job);
    //
    // List<AppNotificationDeliveryEntity> inbox = targets.stream()
    // .map(user -> AppNotificationDeliveryEntity.builder()
    // .notificationId(job.getNotificationId())
    // .userId(user.getUserId())
    // .status(NotificationDeliveryStatus.UNREAD)
    // .dispatchedAt(now)
    // .build())
    // .toList();
    //
    // // insertAll is safe here because the compound unique index on
    // // (notification_id, user_id) prevents duplicates on retry
    // deliveryRepo.insert(inbox);
    //
    // int count = inbox.size();
    // job.setTotalDispatched(count);
    // job.setStatus(NotificationJobStatus.COMPLETED);
    // job.setCompletedAt(System.currentTimeMillis());
    // jobRepo.save(job);
    //
    // // mark the notification as ACTIVE and record dispatch time + count
    // notificationRepo.findById(job.getNotificationId()).ifPresent(n -> {
    // n.setStatus(AppNotificationStatus.ACTIVE);
    // n.setDispatchedAt(now);
    // n.setTotalDispatched(count);
    // n.setTotalRead(0);
    // notificationRepo.save(n);
    // });
    //
    // log.info("NotificationDispatchJob: dispatched notification {} to {} users",
    // job.getNotificationId(), count);
    //
    // } catch (Exception ex) {
    // job.setStatus(NotificationJobStatus.FAILED);
    // job.setErrorMessage(ex.getMessage());
    // job.setCompletedAt(System.currentTimeMillis());
    // jobRepo.save(job);
    // log.error("NotificationDispatchJob: failed for job {}: {}", job.getId(), ex.getMessage(), ex);
    // }
    // }

    // private List<UserAppInstallEntity> resolveTargets(NotificationJobEntity job) {
    // if (job.getTargetType() == NotificationTargetType.SPECIFIC
    // && job.getTargetUserIds() != null
    // && !job.getTargetUserIds().isEmpty()) {
    // return userInstallRepo.findByUserIdInAndIsActiveTrueAndNotificationsEnabledTrue(
    // job.getTargetUserIds());
    // }
    // return userInstallRepo.findByIsActiveTrueAndNotificationsEnabledTrue();
    // }
}
