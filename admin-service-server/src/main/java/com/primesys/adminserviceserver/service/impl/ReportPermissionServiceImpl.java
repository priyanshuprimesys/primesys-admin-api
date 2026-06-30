package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicecommon.dto.report.ReportPermissionDTO;
import com.primesys.adminservicecommon.dto.report.UpdateReportPermissionDTO;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.ResourceNotFoundException;
import com.primesys.adminserviceserver.mapper.reportPermission.ReportPermissionMapper;
import com.primesys.adminserviceserver.service.ReportPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportPermissionServiceImpl implements ReportPermissionService {

    private final DivisionLoginRepository divisionLoginRepository;

    @Override
    public ReportPermissionDTO getReportPermissionById(String id) {
        Optional<DivisionLoginEntity> divisionLoginEntity = divisionLoginRepository.findById(id);
        if (divisionLoginEntity.isEmpty()) {
            throw new ResourceNotFoundException("Division not found");
        }

        ReportPermissionDTO reportPermissionDTOS = ReportPermissionMapper.totDTO(divisionLoginEntity.get());
        return reportPermissionDTOS;
    }

    public ReportPermissionDTO patchModulesList(UpdateReportPermissionDTO updateReportPermissionDTO) {
        DivisionLoginEntity divisionLoginEntity = divisionLoginRepository
                .findById(updateReportPermissionDTO.getDivisionId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Division not found with id: " + updateReportPermissionDTO.getDivisionId()));

        log.info("Report Permission: {}", updateReportPermissionDTO);
        divisionLoginEntity.setModulesList(updateReportPermissionDTO.getModulesList());
        divisionLoginEntity.setLastModified(System.currentTimeMillis() / 1000);
        divisionLoginEntity.setLastModifiedBy(updateReportPermissionDTO.getModifiedBy());
        DivisionLoginEntity saved = divisionLoginRepository.save(divisionLoginEntity);
        ReportPermissionDTO reportPermissionDTO = ReportPermissionMapper.totDTO(saved);
        return reportPermissionDTO;
    }
}
