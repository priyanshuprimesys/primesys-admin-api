package com.primesys.adminserviceserver.modules.profile.services;

import com.primesys.adminservicecommon.dto.DivisionLoginDto;

public interface ProfileService {
    DivisionLoginDto updateProfile(String divisionId, DivisionLoginDto divisionLoginDto);

    DivisionLoginDto getProfile(String divisionId);
}
