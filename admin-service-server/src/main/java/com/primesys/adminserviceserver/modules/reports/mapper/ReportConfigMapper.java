package com.primesys.adminserviceserver.modules.reports.mapper;

import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ModuleMasterDTO;

public final class ReportConfigMapper {

    public static ModuleMasterDTO toDTO(ModuleMasterEntity moduleMasterEntity, boolean active) {
        ModuleMasterDTO masterDTO = new ModuleMasterDTO();
        masterDTO.setId(moduleMasterEntity.getId());
        masterDTO.setStatus(active);
        masterDTO.setModuleName(moduleMasterEntity.getModuleName());
        if (moduleMasterEntity.getSubModules() != null) {
            masterDTO.setSubModules(moduleMasterEntity.getSubModules());
        }
        masterDTO.setDisplayName(moduleMasterEntity.getDisplayName());
        return masterDTO;
    }
}
