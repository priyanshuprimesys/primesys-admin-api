package com.primesys.adminserviceserver.modules.user_app.services.impl;

import com.primesys.adminservicemongodb.entity.UserAppInstallEntity;
import com.primesys.adminservicemongodb.repository.UserAppInstallRepository;
import com.primesys.adminserviceserver.modules.user_app.dtos.UserAppInstallRequestDTO;
import com.primesys.adminserviceserver.modules.user_app.services.UserAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAppServiceImpl implements UserAppService {

    private final UserAppInstallRepository userAppInstallRepository;

    @Override
    public void createUserApp(UserAppInstallRequestDTO dto) {

        if (userAppInstallRepository.existsByDivisionId(dto.divisionId())) {
            return;
        }

        // UserAppInstallEntity userAppInstall

        // userAppInstallRepository.findByDeviceId(dto.deviceId())
        // .ifPresentOrElse(
        // existing -> {
        // existing.setDivisionId(dto.divisionId());
        // existing.setUserName(dto.userName());
        // existing.setMobileNumber(dto.mobileNumber());
        // existing.setPlatform(dto.platform());
        // existing.setAppVersion(dto.appVersion());
        // existing.setFcmToken(dto.fcmToken());
        // existing.setDeviceModel(dto.deviceModel());
        // existing.setDeviceOs(dto.deviceOs());
        // existing.setDeviceVersion(dto.deviceVersion());
        // existing.setLastSeenAt(System.currentTimeMillis());
        // userAppInstallRepository.save(existing);
        // },
        // () -> {
        // UserAppInstallEntity entity = UserAppInstallEntity.builder()
        // .divisionId(dto.divisionId())
        // .userName(dto.userName())
        // .mobileNumber(dto.mobileNumber())
        // .platform(dto.platform())
        // .appVersion(dto.appVersion())
        // .deviceId(dto.deviceId())
        // .fcmToken(dto.fcmToken())
        // .deviceModel(dto.deviceModel())
        // .deviceOs(dto.deviceOs())
        // .deviceVersion(dto.deviceVersion())
        // .isActive(true)
        // .notificationsEnabled(true)
        // .lastSeenAt(System.currentTimeMillis())
        // .build();
        // userAppInstallRepository.save(entity);
        // }
        // );
    }
}
