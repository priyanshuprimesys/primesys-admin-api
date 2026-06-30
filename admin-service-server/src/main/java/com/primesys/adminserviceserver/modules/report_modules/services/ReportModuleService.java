package com.primesys.adminserviceserver.modules.report_modules.services;

import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;
import com.primesys.adminservicemongodb.model.UserReportModuleModel;
import com.primesys.adminserviceserver.modules.report_modules.dtos.*;
import com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport.DivisionCreateReportModule;
import com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport.DivisionReportHierarchyResponseDTO;
import com.primesys.adminserviceserver.modules.report_modules.dtos.divisionReport.DivisionReportModuleResponseDTO;

import java.util.List;

public interface ReportModuleService {
    /// this is for user view only
    List<ReportModuleResponseDTO> getUserReportModules();

    List<ReportModuleResponseDTO> getAllReportModule();

    ReportModuleResponseDTO createReportModule(ReportModuleCreateDTO reportModuleCreateDTO);

    ReportModuleResponseDTO getModuleById(String moduleId);

    ReportModuleResponseDTO updateReportModule(String moduleId, ReportModuleUpdateDTO reportModuleUpdateDTO);

    Boolean destroyModule(String moduleId);

    /// this will update the hierarchy of division report modules
    String createDivisionHierarchyReportModule(String divisionId, List<DivisionReportModuleCreateDTO> reportModules);

    /// this will only update the single division module
    String updateSingleDivisionReportModule(String divisionId, UserReportModuleModel reportModuleModel);

    /// get division and its children with report permissions
    DivisionReportHierarchyResponseDTO getDivisionHierarchyReportModules(String divisionId);

    ModuleMasterEntity updateModuleMaster(ModuleMasterCreateDTO moduleMasterCreateDTO);
}
