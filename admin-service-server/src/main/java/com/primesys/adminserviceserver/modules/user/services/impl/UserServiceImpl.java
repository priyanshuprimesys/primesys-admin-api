package com.primesys.adminserviceserver.modules.user.services.impl;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminserviceserver.modules.user.dtos.UserDTO;
import com.primesys.adminserviceserver.modules.user.mapper.UserMapper;
import com.primesys.adminserviceserver.modules.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final DivisionLoginRepository divisionLoginRepository;

    @Override
    public List<UserDTO> getAllUsers() {
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository.findByIsRailwayUserFalse();
        return divisionLoginEntities.stream().map(UserMapper::toDTO).toList();
    }

    @Override
    public UserDTO getUserById(String id) {
        DivisionLoginEntity divisionLoginEntity = divisionLoginRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserMapper.toDTO(divisionLoginEntity);
    }
}
