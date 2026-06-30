package com.primesys.adminserviceserver.modules.report_modules.mapper;

import com.primesys.adminservicemongodb.entity.ReportModule;
import com.primesys.adminservicemongodb.model.UserReportModuleModel;
import com.primesys.adminserviceserver.modules.report_modules.dtos.DivisionReportModuleCreateDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.ReportModuleCreateDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.ReportModuleResponseDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport.DivisionCreateReportModule;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public final class ReportModuleMapper {

    private ReportModuleMapper() {
    }

    public static UserReportModuleModel toUserReportModuleModel(
            DivisionReportModuleCreateDTO divisionReportModuleCreateDTO) {
        UserReportModuleModel userReportModuleModel = new UserReportModuleModel();
        userReportModuleModel.setActive(divisionReportModuleCreateDTO.active());
        userReportModuleModel.setModuleName(divisionReportModuleCreateDTO.moduleName());
        userReportModuleModel.setDisplayName(divisionReportModuleCreateDTO.displayName());
        userReportModuleModel.setCustomDisplayName(divisionReportModuleCreateDTO.customDisplayName());
        userReportModuleModel.setTypeId(divisionReportModuleCreateDTO.typeId());
        userReportModuleModel.setDisplayOrder(divisionReportModuleCreateDTO.displayOrder());
        userReportModuleModel.setModuleId(divisionReportModuleCreateDTO.id());
        if (divisionReportModuleCreateDTO.parentId() != null && !divisionReportModuleCreateDTO.parentId().isEmpty()) {
            userReportModuleModel.setModuleParentId(divisionReportModuleCreateDTO.parentId());
        }
        if (divisionReportModuleCreateDTO.subModules() != null
                && !divisionReportModuleCreateDTO.subModules().isEmpty()) {
            List<UserReportModuleModel> list = new ArrayList<>();
            for (DivisionReportModuleCreateDTO dto : divisionReportModuleCreateDTO.subModules()) {
                list.add(toUserReportModuleModel(dto));
            }
            userReportModuleModel.setSubModules(list);
        }
        return userReportModuleModel;
    }

    public static ReportModule toEntity(ReportModuleCreateDTO reportModuleCreateDTO) {
        ReportModule reportModule = new ReportModule();
        reportModule.setModuleName(reportModuleCreateDTO.moduleName());
        reportModule.setDescription(reportModuleCreateDTO.description());
        reportModule.setStatus(true);
        if (reportModuleCreateDTO.parentId() != null) {
            reportModule.setParentId(new ObjectId(reportModuleCreateDTO.parentId()));
        }
        reportModule.setDisplayName(reportModuleCreateDTO.displayName());
        reportModule.setDisplayOrder(reportModuleCreateDTO.displayOrder());
        if (reportModuleCreateDTO.typeId() != null) {
            reportModule.setTypeId(reportModuleCreateDTO.typeId());
        }
        return reportModule;
    }

    public static ReportModuleResponseDTO toDTO(ReportModule reportModule) {
        ReportModuleResponseDTO reportModuleResponseDTO = new ReportModuleResponseDTO();
        reportModuleResponseDTO.setId(reportModule.getId().toString());
        reportModuleResponseDTO.setModuleName(reportModule.getModuleName());
        reportModuleResponseDTO.setDescription(reportModule.getDescription());
        reportModuleResponseDTO.setActive(reportModule.getStatus());
        if (reportModule.getParentId() != null) {
            reportModuleResponseDTO.setParentId(reportModule.getParentId().toString());
        }
        reportModuleResponseDTO.setDisplayName(reportModule.getDisplayName());
        reportModuleResponseDTO.setDisplayOrder(reportModule.getDisplayOrder());
        if (reportModule.getTypeId() != null) {
            reportModuleResponseDTO.setTypeId(reportModule.getTypeId());
        }
        return reportModuleResponseDTO;
    }

    public static ReportModuleResponseDTO toReportModuleResponse(UserReportModuleModel userReportModuleModel) {
        ReportModuleResponseDTO reportModuleResponseDTO = new ReportModuleResponseDTO();
        reportModuleResponseDTO.setActive(userReportModuleModel.getActive());
        reportModuleResponseDTO.setDisplayOrder(userReportModuleModel.getDisplayOrder());
        if (userReportModuleModel.getModuleParentId() != null && !userReportModuleModel.getModuleParentId().isEmpty()) {
            reportModuleResponseDTO.setParentId(userReportModuleModel.getModuleParentId());
        }
        if (userReportModuleModel.getSubModules() != null && !userReportModuleModel.getSubModules().isEmpty()) {
            List<ReportModuleResponseDTO> list = new ArrayList<>();
            for (UserReportModuleModel dto : userReportModuleModel.getSubModules()) {
                list.add(toReportModuleResponse(dto));
            }
            reportModuleResponseDTO.setSubModules(list);
        }
        reportModuleResponseDTO.setId(userReportModuleModel.getModuleId());
        reportModuleResponseDTO.setModuleName(userReportModuleModel.getModuleName());
        reportModuleResponseDTO.setCustomDisplayName(userReportModuleModel.getCustomDisplayName());
        reportModuleResponseDTO.setTypeId(userReportModuleModel.getTypeId());
        return reportModuleResponseDTO;
    }

    public static ReportModuleResponseDTO toUserDTO(ReportModule reportModule) {
        ReportModuleResponseDTO reportModuleResponseDTO = new ReportModuleResponseDTO();
        reportModuleResponseDTO.setModuleName(reportModule.getModuleName());
        reportModuleResponseDTO.setDisplayName(reportModule.getDisplayName());
        reportModuleResponseDTO.setDisplayOrder(reportModule.getDisplayOrder());
        if (reportModule.getTypeId() != null) {
            reportModuleResponseDTO.setTypeId(reportModule.getTypeId());
        }
        return reportModuleResponseDTO;
    }

    public static ReportModuleResponseDTO moduleToCustomDTO(ReportModule reportModule,
            UserReportModuleModel userReportModuleModel) {
        ReportModuleResponseDTO custom = new ReportModuleResponseDTO();
        if (userReportModuleModel.getCustomDisplayName() != null) {
            custom.setCustomDisplayName(userReportModuleModel.getCustomDisplayName());
        }
        if (reportModule.getDescription() != null) {
            custom.setDescription(reportModule.getDescription());
        }
        custom.setModuleName(reportModule.getModuleName());
        custom.setDisplayName(reportModule.getDisplayName());
        custom.setParentId(reportModule.getParentId() != null ? reportModule.getParentId().toHexString() : null);
        // custom.setStatus(reportModule.getStatus());
        custom.setDisplayOrder(reportModule.getDisplayOrder());
        if (userReportModuleModel.getTypeId() != null) {
            custom.setCustomTypeId(userReportModuleModel.getTypeId());
        }
        custom.setId(reportModule.getId().toString());
        custom.setActive(userReportModuleModel.getActive());
        return custom;
    }
}
