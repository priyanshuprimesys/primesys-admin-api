package com.primesys.adminserviceserver.modules.location.mapper;

import com.primesys.adminservicemongodb.entity.DeviceHistoryEntity;
import com.primesys.adminservicemongodb.entity.LocationBackupEntity;
import com.primesys.adminserviceserver.modules.location.dtos.LocationResponseDTO;

public final class LocationBackupMapper {

    public static LocationBackupEntity toEntity(DeviceHistoryEntity deviceHistory) {
        LocationBackupEntity locationBackupEntity = new LocationBackupEntity();
        locationBackupEntity.setBlind(deviceHistory.getBlind());
        locationBackupEntity.setGeoLocation(deviceHistory.getGeoLocation());
        locationBackupEntity.setSpeed(deviceHistory.getSpeed());
        locationBackupEntity.setStatus(deviceHistory.getStatus());
        locationBackupEntity.setBlindReceivedAt(deviceHistory.getBlindReceivedAt());
        locationBackupEntity.setDeviceImei(deviceHistory.getDeviceImei());
        locationBackupEntity.setGsmSignalStrength(deviceHistory.getGsmSignalStrength());
        locationBackupEntity.setNearestRdps(deviceHistory.getNearestRdps());
        locationBackupEntity.setSatelliteNo(deviceHistory.getSatelliteNo());
        locationBackupEntity.setTimestamp(deviceHistory.getTimestamp());
        locationBackupEntity.setVoltageLevel(deviceHistory.getVoltageLevel());
        return locationBackupEntity;
    }

    public static DeviceHistoryEntity toDeviceEntity(LocationBackupEntity locationBackupEntity) {
        DeviceHistoryEntity deviceHistoryEntity = new DeviceHistoryEntity();
        deviceHistoryEntity.setBlind(locationBackupEntity.getBlind());
        deviceHistoryEntity.setGeoLocation(locationBackupEntity.getGeoLocation());
        deviceHistoryEntity.setSpeed(locationBackupEntity.getSpeed());
        deviceHistoryEntity.setStatus(locationBackupEntity.getStatus());
        deviceHistoryEntity.setBlindReceivedAt(locationBackupEntity.getBlindReceivedAt());
        deviceHistoryEntity.setDeviceImei(locationBackupEntity.getDeviceImei());
        deviceHistoryEntity.setGsmSignalStrength(locationBackupEntity.getGsmSignalStrength());
        deviceHistoryEntity.setNearestRdps(locationBackupEntity.getNearestRdps());
        deviceHistoryEntity.setSatelliteNo(locationBackupEntity.getSatelliteNo());
        deviceHistoryEntity.setTimestamp(locationBackupEntity.getTimestamp());
        deviceHistoryEntity.setVoltageLevel(locationBackupEntity.getVoltageLevel());
        return deviceHistoryEntity;
    }

    public static DeviceHistoryEntity toEntityWithSecondsDiff(DeviceHistoryEntity deviceHistory, Long timestampDiff) {
        DeviceHistoryEntity deviceHistoryEntity = new DeviceHistoryEntity();
        deviceHistoryEntity.setBlind(deviceHistory.getBlind());
        deviceHistoryEntity.setGeoLocation(deviceHistory.getGeoLocation());
        deviceHistoryEntity.setSpeed(deviceHistory.getSpeed());
        deviceHistoryEntity.setStatus(deviceHistory.getStatus());
        deviceHistoryEntity.setBlindReceivedAt(deviceHistory.getBlindReceivedAt());
        deviceHistoryEntity.setDeviceImei(deviceHistory.getDeviceImei());
        deviceHistoryEntity.setGsmSignalStrength(deviceHistory.getGsmSignalStrength());
        deviceHistoryEntity.setNearestRdps(deviceHistory.getNearestRdps());
        deviceHistoryEntity.setSatelliteNo(deviceHistory.getSatelliteNo());
        deviceHistoryEntity.setTimestamp(deviceHistory.getTimestamp() + timestampDiff);
        deviceHistoryEntity.setVoltageLevel(deviceHistory.getVoltageLevel());
        return deviceHistoryEntity;
    }

    public static LocationResponseDTO toDTO(LocationBackupEntity e) {
        LocationResponseDTO dto = new LocationResponseDTO();

        dto.setDeviceImei(e.getDeviceImei());
        dto.setTimestamp(e.getTimestamp());
        dto.setSpeed(e.getSpeed());

        // geo location (lon, lat)
        if (e.getGeoLocation() != null && e.getGeoLocation().getCoordinates() != null
                && e.getGeoLocation().getCoordinates().size() >= 2) {
            dto.setLon(e.getGeoLocation().getCoordinates().get(0));
            dto.setLat(e.getGeoLocation().getCoordinates().get(1));
        }

        // status directions
        if (e.getStatus() != null) {
            dto.setLatDirection(e.getStatus().getLatDirection());
            dto.setLonDirection(e.getStatus().getLonDirection());
        }

        // these are not in entity → keep null or set separately later
        dto.setDeviceUserType(null);
        dto.setName(null);
        return dto;
    }
}
