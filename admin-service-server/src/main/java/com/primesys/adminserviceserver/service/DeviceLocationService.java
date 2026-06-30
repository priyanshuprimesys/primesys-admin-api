package com.primesys.adminserviceserver.service;

import com.primesys.adminserviceserver.dtos.location.DeviceLocationDTO;
import com.primesys.adminserviceserver.dtos.location.DeviceLocationRequestDTO;

import java.util.List;

public interface DeviceLocationService {
    List<DeviceLocationDTO> getAllLocationBetweenTimestamp(DeviceLocationRequestDTO deviceLocationRequestDTO);

    String deleteAllLocations(Long imei, List<Long> timestamps, String deletedBy);
}
