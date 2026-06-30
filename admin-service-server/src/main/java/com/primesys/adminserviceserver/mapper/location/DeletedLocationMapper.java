package com.primesys.adminserviceserver.mapper.location;

import com.primesys.adminservicemongodb.entity.LocationOutOfIndia;
import com.primesys.adminserviceserver.dtos.location.CreateDeletedLocationDTO;

public final class DeletedLocationMapper {

    public LocationOutOfIndia toEntity(CreateDeletedLocationDTO dto, String deletedBy) {
        LocationOutOfIndia locationOutOfIndia = new LocationOutOfIndia();
        locationOutOfIndia.setDeletedBy(deletedBy);
        locationOutOfIndia.setGeoLocation(dto.deviceLocation().getGeoLocation());
        locationOutOfIndia.setBlind(dto.deviceLocation().getBlind());
        locationOutOfIndia.setStatus(dto.deviceLocation().getStatus());
        locationOutOfIndia.setSpeed(dto.deviceLocation().getSpeed());
        locationOutOfIndia.setBlindReceivedAt(dto.deviceLocation().getBlindReceivedAt());
        locationOutOfIndia.setNearestRdps(dto.deviceLocation().getNearestRdps());
        locationOutOfIndia.setGsmSignalStrength(dto.deviceLocation().getGsmSignalStrength());
        locationOutOfIndia.setTimestamp(dto.deviceLocation().getTimestamp());
        locationOutOfIndia.setDeviceImei(dto.deviceLocation().getDeviceImei());
        locationOutOfIndia.setSatelliteNo(dto.deviceLocation().getSatelliteNo());
        locationOutOfIndia.setVoltageLevel(dto.deviceLocation().getVoltageLevel());
        return locationOutOfIndia;
    }
}
