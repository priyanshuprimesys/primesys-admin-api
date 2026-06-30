package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.UserAppInstallEntity;
import com.primesys.adminservicemongodb.enums.AppPlatform;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAppInstallRepository extends MongoRepository<UserAppInstallEntity, String> {

    Optional<UserAppInstallEntity> findByDeviceId(String deviceId);

    /** ALL broadcast — every active user who hasn't opted out */
    List<UserAppInstallEntity> findByIsActiveTrueAndNotificationsEnabledTrue();

    /** Total install base count shown on the admin dashboard */
    long countByIsActiveTrue();

    List<UserAppInstallEntity> findByPlatformAndIsActiveTrue(AppPlatform platform);

    /** Find active users on a specific OS version — used to target incompatible-version notifications */
    List<UserAppInstallEntity> findByDeviceVersionAndIsActiveTrueAndNotificationsEnabledTrue(String deviceVersion);

    /** Find active users whose OS version is in the given list (bulk incompatibility check) */
    List<UserAppInstallEntity> findByDeviceVersionInAndIsActiveTrueAndNotificationsEnabledTrue(
            List<String> deviceVersions);

    boolean existsByDeviceId(String deviceId);

    boolean existsByDivisionId(String divisionId);
}
