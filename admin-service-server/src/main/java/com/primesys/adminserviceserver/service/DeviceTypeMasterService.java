package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.DeviceTypeMasterEntity;
import com.primesys.adminservicemongodb.repository.DeviceTypeMasterRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DeviceTypeMasterService {

    private final DeviceTypeMasterRepository repository;

    public DeviceTypeMasterService(DeviceTypeMasterRepository repository) {
        this.repository = repository;
    }

    public List<DeviceTypeMasterEntity> getAllDeviceTypes() {
        return repository.findAll(); // same as db.device_type_master.find({})
    }
}
