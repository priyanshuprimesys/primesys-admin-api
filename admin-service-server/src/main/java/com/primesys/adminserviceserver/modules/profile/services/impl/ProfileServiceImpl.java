package com.primesys.adminserviceserver.modules.profile.services.impl;

import com.primesys.adminservicecommon.dto.DivisionLoginDto;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminserviceserver.modules.profile.mapper.ProfileMapper;
import com.primesys.adminserviceserver.modules.profile.services.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final DivisionLoginRepository divisionLoginRepository;

    @Override
    public DivisionLoginDto updateProfile(String divisionId, DivisionLoginDto divisionLoginDto) {
        DivisionLoginEntity divisionLoginEntity = divisionLoginRepository.findById(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("Profile does not exists"));
        ProfileMapper.updateEntity(divisionLoginEntity, divisionLoginDto);
        DivisionLoginEntity divisionLogin = divisionLoginRepository.save(divisionLoginEntity);
        return ProfileMapper.toDTO(divisionLogin);
    }

    @Override
    public DivisionLoginDto getProfile(String divisionId) {
        DivisionLoginEntity divisionLoginEntity = divisionLoginRepository.findByUserName(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("Profile does not exists"));
        return ProfileMapper.toDTO(divisionLoginEntity);
    }
}
