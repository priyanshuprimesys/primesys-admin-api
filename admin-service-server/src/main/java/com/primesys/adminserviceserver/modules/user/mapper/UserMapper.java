package com.primesys.adminserviceserver.modules.user.mapper;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminserviceserver.modules.user.dtos.UserDTO;

public final class UserMapper {

    public static UserDTO toDTO(DivisionLoginEntity loginEntity) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(loginEntity.getId());
        userDTO.setParentId(loginEntity.getParentId());
        userDTO.setUserLoginId(loginEntity.getUserLoginId());
        userDTO.setUserName(loginEntity.getUserName());
        userDTO.setName(loginEntity.getName());
        userDTO.setMobileNo(loginEntity.getMobileNo());
        userDTO.setRoleId(loginEntity.getRoleId());
        userDTO.setDeptId(loginEntity.getDeptId());
        userDTO.setPath(loginEntity.getPath());
        userDTO.setTrackDivisionId(loginEntity.getTrackDivisionId());
        userDTO.setActiveStatus(loginEntity.getActiveStatus());
        return userDTO;
    }
}
