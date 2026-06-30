package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicemongodb.entity.DeviceLocation;
import com.primesys.adminservicemongodb.entity.LocationOutOfIndia;
import com.primesys.adminservicemongodb.repository.LocationOutOfIndiaRepository;
import com.primesys.adminserviceserver.service.LocationOutOfIndiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class LocationOutOfIndiaOutOfIndiaServiceImpl implements LocationOutOfIndiaService {

    private final LocationOutOfIndiaRepository locationOutOfIndiaRepository;

    @Override
    public boolean createDeletedLocation(List<DeviceLocation> deviceLocations, String deletedBy) {
        deviceLocations.forEach(location -> {
            LocationOutOfIndia locationOutOfIndia = new LocationOutOfIndia();
            locationOutOfIndia.setDeletedBy(deletedBy);
            locationOutOfIndia.setGeoLocation(location.getGeoLocation());
            locationOutOfIndia.setBlind(location.getBlind());
            locationOutOfIndia.setSpeed(location.getSpeed());
            locationOutOfIndia.setDeviceImei(location.getDeviceImei());
            locationOutOfIndia.setTimestamp(location.getTimestamp());
            locationOutOfIndia.setNearestRdps(location.getNearestRdps());
            locationOutOfIndia.setStatus(location.getStatus());
            locationOutOfIndia.setSatelliteNo(location.getSatelliteNo());
            locationOutOfIndia.setVoltageLevel(location.getVoltageLevel());
            locationOutOfIndia.setBlindReceivedAt(location.getBlindReceivedAt());
            locationOutOfIndia.setGsmSignalStrength(location.getGsmSignalStrength());
            locationOutOfIndiaRepository.save(locationOutOfIndia);
        });

        return true;
    }
}
