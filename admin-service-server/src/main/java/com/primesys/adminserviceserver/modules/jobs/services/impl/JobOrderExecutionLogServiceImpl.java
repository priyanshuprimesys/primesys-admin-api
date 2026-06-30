package com.primesys.adminserviceserver.modules.jobs.services.impl;

import com.primesys.adminservicemongodb.entity.JobOrderExecutionLogEntity;
import com.primesys.adminservicemongodb.repository.JobOrderExecutionLogRepository;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.BadRequestException;
import com.primesys.adminserviceserver.modules.jobs.dtos.JobOrderExecutionLogCreateDTO;
import com.primesys.adminserviceserver.modules.jobs.dtos.JobOrderExecutionLogDTO;
import com.primesys.adminserviceserver.modules.jobs.mapper.JobOrderExecutionLogMapper;
import com.primesys.adminserviceserver.modules.jobs.services.JobOrderExecutionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobOrderExecutionLogServiceImpl implements JobOrderExecutionLogService {
    private final JobOrderExecutionLogRepository jobOrderExecutionLogRepository;

    public JobOrderExecutionLogDTO create(JobOrderExecutionLogCreateDTO jobOrderExecutionLogCreateDTO) {
        try {
            JobOrderExecutionLogEntity jobMap = JobOrderExecutionLogMapper.toEntity(jobOrderExecutionLogCreateDTO);
            JobOrderExecutionLogEntity createLog = jobOrderExecutionLogRepository.save(jobMap);
            return JobOrderExecutionLogMapper.toDTO(createLog);
        } catch (RuntimeException e) {
            throw new BadRequestException("Job Execution log not created");
        }
    }
}
