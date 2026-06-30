package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;
import com.primesys.adminservicemongodb.repository.ModuleMasterRepository;
import com.primesys.adminserviceserver.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    public final ModuleMasterRepository moduleRepository;

    public List<ModuleMasterEntity> getAllModules() {
        return moduleRepository.findAll();
    }

    public ModuleMasterEntity getModuleById(Long id) {
        return moduleRepository.findById(String.valueOf(id)).orElse(null);
    }
}
