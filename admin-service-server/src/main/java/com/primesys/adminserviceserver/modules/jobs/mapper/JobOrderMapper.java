package com.primesys.adminserviceserver.modules.jobs.mapper;

import com.primesys.adminservicemongodb.entity.JobOrderEntity;
import com.primesys.adminservicemongodb.enums.JobOrderStatus;
import com.primesys.adminserviceserver.modules.jobs.dtos.jobs.JobOrderCreateDTO;
import com.primesys.adminserviceserver.modules.jobs.dtos.jobs.JobOrderDTO;
import com.primesys.adminserviceserver.utility.DateTimeUtility;

public final class JobOrderMapper {

    public static JobOrderEntity toEntity(JobOrderCreateDTO jobOrderCreateDTO) {
        JobOrderEntity jobOrderEntity = new JobOrderEntity();
        jobOrderEntity.setJobName(jobOrderCreateDTO.jobName());
        jobOrderEntity.setCreatedBy(jobOrderCreateDTO.createdBy());
        jobOrderEntity.setEndAt(DateTimeUtility.StringToInstant(jobOrderCreateDTO.endDateAt().toString()));
        jobOrderEntity.setStatus(jobOrderCreateDTO.status());
        jobOrderEntity.setStartFrom(DateTimeUtility.StringToInstant(jobOrderCreateDTO.startDateFrom().toString()));
        jobOrderEntity.setUpdatedBy(jobOrderCreateDTO.createdBy());
        jobOrderEntity.setTrackDivisionIds(jobOrderCreateDTO.trackDivisionIds());
        jobOrderEntity.setTypeId(jobOrderCreateDTO.typeId());
        return jobOrderEntity;
    }

    public static JobOrderDTO toJobDTO(JobOrderEntity jobOrderEntity) {
        JobOrderDTO jobOrderDTO = new JobOrderDTO();
        jobOrderDTO.setId(jobOrderEntity.getId());
        jobOrderDTO.setCreatedBy(jobOrderDTO.getCreatedBy());
        jobOrderDTO.setJobName(jobOrderEntity.getJobName());
        jobOrderDTO.setStatus(JobOrderStatus.valueOf(jobOrderEntity.getStatus().toString()));
        jobOrderDTO.setHoldUpto(jobOrderEntity.getHoldUpto());
        jobOrderDTO.setEndAt(jobOrderEntity.getEndAt());
        jobOrderDTO.setStartFrom(jobOrderEntity.getStartFrom());
        jobOrderDTO.setTrackDivisionIds(jobOrderEntity.getTrackDivisionIds());
        jobOrderDTO.setTypeId(jobOrderEntity.getTypeId());
        return jobOrderDTO;
    }
}
