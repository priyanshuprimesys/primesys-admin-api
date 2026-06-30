package com.primesys.adminserviceserver.modules.jobs.services.impl;

import com.primesys.adminservicemongodb.entity.JobOrderEntity;
import com.primesys.adminservicemongodb.repository.JobOrderRepository;
import com.primesys.adminserviceserver.modules.jobs.dtos.jobs.JobOrderCreateDTO;
import com.primesys.adminserviceserver.modules.jobs.dtos.jobs.JobOrderDTO;
import com.primesys.adminserviceserver.modules.jobs.mapper.JobOrderMapper;
import com.primesys.adminserviceserver.modules.jobs.services.JobOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobOrderServiceImpl implements JobOrderService {

    private final JobOrderRepository jobOrderRepository;

    public List<JobOrderDTO> getAll() {
        List<JobOrderEntity> jobOrderEntities = jobOrderRepository.findAll();
        List<JobOrderDTO> jobOrderDTOS = jobOrderEntities.stream().map(JobOrderMapper::toJobDTO).toList();
        return jobOrderDTOS;
    }

    public JobOrderDTO create(JobOrderCreateDTO jobOrderCreateDTO) {
        try {
            JobOrderEntity jobMap = JobOrderMapper.toEntity(jobOrderCreateDTO);
            jobMap.setCreatedAt(Instant.now());
            jobMap.setUpdatedAt(Instant.now());
            JobOrderEntity createJob = jobOrderRepository.save(jobMap);
            return JobOrderMapper.toJobDTO(createJob);
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception with JobOrders " + e.getMessage());
        }
    }
}
