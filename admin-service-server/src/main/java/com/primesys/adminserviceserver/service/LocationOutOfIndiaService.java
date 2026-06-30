package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.DeviceLocation;

import java.util.List;

public interface LocationOutOfIndiaService {
    boolean createDeletedLocation(List<DeviceLocation> deviceLocations, String deletedBy);
}
