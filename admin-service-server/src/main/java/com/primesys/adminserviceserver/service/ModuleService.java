package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.ModuleMasterEntity;

import java.util.List;

public interface ModuleService {

    public List<ModuleMasterEntity> getAllModules();

    public ModuleMasterEntity getModuleById(Long id);

}
