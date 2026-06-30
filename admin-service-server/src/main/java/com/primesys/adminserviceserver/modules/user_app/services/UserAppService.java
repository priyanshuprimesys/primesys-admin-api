package com.primesys.adminserviceserver.modules.user_app.services;

import com.primesys.adminserviceserver.modules.user_app.dtos.UserAppInstallRequestDTO;

public interface UserAppService {

    void createUserApp(UserAppInstallRequestDTO dto);
}
