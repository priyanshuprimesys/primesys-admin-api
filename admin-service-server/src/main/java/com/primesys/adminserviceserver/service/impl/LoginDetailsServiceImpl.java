package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicecommon.dto.DivisionLoginDto;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminserviceserver.config.properties.PrimeSysProperties;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.ResourceNotFoundException;
import com.primesys.adminserviceserver.service.LoginDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginDetailsServiceImpl implements LoginDetailsService {

    private final DivisionLoginRepository divisionLoginRepository;
    private final PrimeSysProperties primesysProperties;

    @Override
    public DivisionLoginDto getLoginDetails(final String userName) {
        final Optional<DivisionLoginEntity> dLEntity = divisionLoginRepository.findByUserName(userName);
        if (dLEntity.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());

        return DivisionLoginDto.builder().roleId(dLEntity.get().getRoleId()).emailID(dLEntity.get().getUsername())
                .userName(dLEntity.get().getName()).mobileNo(dLEntity.get().getMobileNo())
                .divisionId(dLEntity.get().getId()).socketUrl(primesysProperties.getSocketUrl())
                .distUnit(primesysProperties.getDistUnit()).build();
    }
}
