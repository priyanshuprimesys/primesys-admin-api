package com.primesys.adminserviceserver.mapper.reportPermission;

import com.primesys.adminservicecommon.dto.report.ReportPermissionDTO;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;

public final class ReportPermissionMapper {
    public static ReportPermissionDTO totDTO(DivisionLoginEntity divisionLoginEntity) {
        ReportPermissionDTO reportPermissionDTO = new ReportPermissionDTO();
        reportPermissionDTO.setId(divisionLoginEntity.getId());
        reportPermissionDTO.setName(divisionLoginEntity.getName());
        reportPermissionDTO.setDeptId(divisionLoginEntity.getDeptId());
        reportPermissionDTO.setTrackDivisionId(divisionLoginEntity.getTrackDivisionId());
        reportPermissionDTO.setModuleList(divisionLoginEntity.getModulesList());
        reportPermissionDTO.setUserName(divisionLoginEntity.getUserName());
        return reportPermissionDTO;
    }
}
