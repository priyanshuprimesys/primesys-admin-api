package com.primesys.adminserviceserver.mapper;

import com.primesys.adminservicecommon.dto.division.DivisionListDTO;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;

public final class DivisionMapper {
    public static DivisionListDTO toDivisionListDTO(DivisionLoginEntity divisionLoginEntity) {
        DivisionListDTO dto = new DivisionListDTO();
        dto.setId(divisionLoginEntity.getId());
        dto.setName(divisionLoginEntity.getName());
        dto.setDeptId(divisionLoginEntity.getDeptId());
        dto.setUserName(divisionLoginEntity.getUserName());
        dto.setTrackDivisionId(divisionLoginEntity.getTrackDivisionId());
        dto.setRoleId(divisionLoginEntity.getRoleId());
        return dto;
    }
}
