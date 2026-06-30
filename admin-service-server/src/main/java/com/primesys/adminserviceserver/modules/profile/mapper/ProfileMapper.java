package com.primesys.adminserviceserver.modules.profile.mapper;

import com.primesys.adminservicecommon.dto.DivisionLoginDto;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;

public final class ProfileMapper {

    public static void updateEntity(DivisionLoginEntity divisionLoginEntity, DivisionLoginDto userLogin) {

        if (userLogin.getEmailID() != null) {
            divisionLoginEntity.setUserName(userLogin.getEmailID());
        }

        if (userLogin.getMobileNo() != null) {
            divisionLoginEntity.setMobileNo(userLogin.getMobileNo());
        }

        if (userLogin.getUserName() != null) {
            divisionLoginEntity.setName(userLogin.getUserName());
        }

        if (userLogin.getPassword() != null) {
            divisionLoginEntity.setPassword(userLogin.getPassword());
        }
    }

    public static DivisionLoginDto toDTO(DivisionLoginEntity divisionLogin) {
        DivisionLoginDto divisionLoginDto = new DivisionLoginDto();
        divisionLoginDto.setUserName(divisionLogin.getName());
        divisionLoginDto.setEmailID(divisionLogin.getUserName());
        divisionLoginDto.setMobileNo(divisionLogin.getMobileNo());
        divisionLoginDto.setRoleId(divisionLogin.getRoleId());
        divisionLoginDto.setDivisionId(divisionLogin.getTrackDivisionId());
        return divisionLoginDto;
    }
}
