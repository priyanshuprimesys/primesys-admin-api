package com.primesys.adminserviceserver.modules.jobs.services;

import com.primesys.adminserviceserver.modules.jobs.dtos.jobs.JobOrderCreateDTO;
import com.primesys.adminserviceserver.modules.jobs.dtos.jobs.JobOrderDTO;

import java.util.List;

public interface JobOrderService {
    JobOrderDTO create(JobOrderCreateDTO jobOrderCreateDTO);

    /// get all job orders
    List<JobOrderDTO> getAll();
}
