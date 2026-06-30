package com.primesys.adminserviceserver.modules.jobs.services;

import com.primesys.adminserviceserver.modules.jobs.dtos.JobOrderExecutionLogCreateDTO;
import com.primesys.adminserviceserver.modules.jobs.dtos.JobOrderExecutionLogDTO;

public interface JobOrderExecutionLogService {
    JobOrderExecutionLogDTO create(JobOrderExecutionLogCreateDTO jobOrderExecutionLogCreateDTO);
}
