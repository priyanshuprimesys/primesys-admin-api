package com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModuleMasterDTO {
    String id;
    String moduleName;
    String displayName;
    private List<ModuleMasterEntity> subModules;
    private Boolean status;
}
