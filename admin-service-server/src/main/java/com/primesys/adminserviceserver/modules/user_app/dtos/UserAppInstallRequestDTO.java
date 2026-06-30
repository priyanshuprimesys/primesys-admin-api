package com.primesys.adminserviceserver.modules.user_app.dtos;

import com.primesys.adminservicemongodb.enums.AppPlatform;

public record UserAppInstallRequestDTO(String divisionId, AppPlatform platform, String appVersion, String deviceId,
        String fcmToken, String deviceVersion, Boolean notificationsEnabled) {
}
