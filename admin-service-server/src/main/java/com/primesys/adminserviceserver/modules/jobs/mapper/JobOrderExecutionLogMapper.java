package com.primesys.adminserviceserver.modules.jobs.mapper;

import com.primesys.adminservicemongodb.entity.JobOrderExecutionLogEntity;
import com.primesys.adminservicemongodb.enums.JobOrderExecutionStatus;
import com.primesys.adminserviceserver.modules.jobs.dtos.JobOrderExecutionLogCreateDTO;
import com.primesys.adminserviceserver.modules.jobs.dtos.JobOrderExecutionLogDTO;

public final class JobOrderExecutionLogMapper {

    public static JobOrderExecutionLogEntity toEntity(JobOrderExecutionLogCreateDTO jobOrderExecutionLogCreateDTO) {
        JobOrderExecutionLogEntity jobOrderExecutionLogEntity = new JobOrderExecutionLogEntity();
        jobOrderExecutionLogEntity.setJobOrderId(jobOrderExecutionLogCreateDTO.getJobOrderId());
        jobOrderExecutionLogEntity.setJobEndAt(jobOrderExecutionLogCreateDTO.getJobEndAt());
        jobOrderExecutionLogEntity.setJobOrderTypeId(jobOrderExecutionLogCreateDTO.getJobOrderTypeId());
        jobOrderExecutionLogEntity.setJobStartAt(jobOrderExecutionLogCreateDTO.getJobStartAt());
        if (jobOrderExecutionLogCreateDTO.getCreatedBy() == null
                || jobOrderExecutionLogCreateDTO.getCreatedBy().isBlank()) {
            jobOrderExecutionLogEntity.setCreatedBy("SYSTEM");
            jobOrderExecutionLogEntity.setUpdatedBy("SYSTEM");
        } else {
            jobOrderExecutionLogEntity.setCreatedBy(jobOrderExecutionLogCreateDTO.getCreatedBy());
            jobOrderExecutionLogEntity.setUpdatedBy(jobOrderExecutionLogCreateDTO.getCreatedBy());
        }
        jobOrderExecutionLogEntity.setTrackDivisionId(jobOrderExecutionLogCreateDTO.getTrackDivisionId());
        jobOrderExecutionLogEntity
                .setStatus(JobOrderExecutionStatus.valueOf(jobOrderExecutionLogCreateDTO.getStatus().toUpperCase()));
        jobOrderExecutionLogEntity.setFailedDevices(jobOrderExecutionLogCreateDTO.getFailedDevices());
        jobOrderExecutionLogEntity.setRemark(jobOrderExecutionLogCreateDTO.getRemark());
        return jobOrderExecutionLogEntity;
    }

    public static JobOrderExecutionLogDTO toDTO(JobOrderExecutionLogEntity jobOrderExecutionLogEntity) {
        JobOrderExecutionLogDTO jobOrderExecutionLogDTO = new JobOrderExecutionLogDTO();
        jobOrderExecutionLogDTO.setId(jobOrderExecutionLogEntity.getId());
        jobOrderExecutionLogDTO.setJobOrderId(jobOrderExecutionLogEntity.getJobOrderId());
        jobOrderExecutionLogDTO.setJobEndAt(jobOrderExecutionLogEntity.getJobEndAt());
        jobOrderExecutionLogDTO.setJobOrderTypeId(jobOrderExecutionLogEntity.getJobOrderTypeId());
        jobOrderExecutionLogDTO.setJobStartAt(jobOrderExecutionLogEntity.getJobStartAt());
        jobOrderExecutionLogDTO.setCreatedBy(jobOrderExecutionLogEntity.getCreatedBy());
        jobOrderExecutionLogDTO.setUpdatedBy(jobOrderExecutionLogEntity.getUpdatedBy());
        jobOrderExecutionLogDTO.setTrackDivisionId(jobOrderExecutionLogEntity.getTrackDivisionId());
        jobOrderExecutionLogDTO.setStatus(jobOrderExecutionLogEntity.getStatus());
        jobOrderExecutionLogDTO.setFailedDevices(jobOrderExecutionLogEntity.getFailedDevices());
        jobOrderExecutionLogDTO.setRemark(jobOrderExecutionLogEntity.getRemark());
        return jobOrderExecutionLogDTO;
    }

}
