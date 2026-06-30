package com.primesys.adminserviceserver.service;

import com.primesys.adminservicecommon.dto.DivisionLoginDto;

public interface LoginDetailsService {
    DivisionLoginDto getLoginDetails(String userName);
}
