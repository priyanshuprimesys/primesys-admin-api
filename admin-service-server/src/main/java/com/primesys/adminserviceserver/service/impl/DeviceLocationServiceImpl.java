package com.primesys.adminserviceserver.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primesys.adminservicemongodb.entity.DeviceLocation;
import com.primesys.adminservicemongodb.model.NearestRdps;
import com.primesys.adminservicemongodb.repository.DeviceLocationRepository;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminserviceserver.dtos.location.DeviceLocationDTO;
import com.primesys.adminserviceserver.dtos.location.DeviceLocationRequestDTO;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.ResourceNotFoundException;
import com.primesys.adminserviceserver.service.DeviceLocationService;
import com.primesys.adminserviceserver.service.DivisionLoginService;
import com.primesys.adminserviceserver.service.LocationOutOfIndiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceLocationServiceImpl implements DeviceLocationService {

    private final DeviceLocationRepository deviceLocationRepository;
    private final DivisionLoginRepository divisionLoginRepository;
    private final ObjectMapper objectMapper;
    private final LocationOutOfIndiaService locationOutOfIndiaService;

    @Override
    public List<DeviceLocationDTO> getAllLocationBetweenTimestamp(DeviceLocationRequestDTO deviceLocationRequestDTO) {
        List<DeviceLocation> deviceLocations = deviceLocationRepository.findByDeviceImeiAndTimestampBetween(
                deviceLocationRequestDTO.imei(), deviceLocationRequestDTO.startTime(),
                deviceLocationRequestDTO.endTime());
        if (deviceLocations == null) {
            throw new ResourceNotFoundException("Locations not found");
        }

        return deviceLocations.stream().map(deviceLocation -> {
            String featureDetail = "data_not_found";
            double dist = 0;
            String rdpsKm = "";

            if (deviceLocation.getNearestRdps() != null
                    && !deviceLocation.getNearestRdps().toString().equals("data_not_found")) {
                ObjectMapper mapper = new ObjectMapper();
                NearestRdps nearestRdps = mapper.convertValue(deviceLocation.getNearestRdps(), NearestRdps.class);

                featureDetail = nearestRdps.getFeatureDetail();

                DecimalFormat df = new DecimalFormat("#.00");

                double kmValue = nearestRdps.getKilometer();
                double kmPlusDistance = kmValue + (nearestRdps.getDistance() / 1000.0);

                rdpsKm = kmValue + "/" + nearestRdps.getDistance() + " [" + df.format(kmPlusDistance) + "]";

                dist = Math.round(nearestRdps.getDistanceDiff() * 100.0) / 100.0;
            }

            return DeviceLocationDTO.builder().timestamp(deviceLocation.getTimestamp()).speed(deviceLocation.getSpeed())
                    .isBlind(deviceLocation.getBlind()).lon(deviceLocation.getGeoLocation().getCoordinates().get(0))
                    .lat(deviceLocation.getGeoLocation().getCoordinates().get(1))
                    .blindLocationGetTimestamp(deviceLocation.getBlindReceivedAt())
                    .lonDirection(deviceLocation.getStatus().getLonDirection())
                    .latDirection(deviceLocation.getStatus().getLatDirection()).featureDetail(featureDetail)
                    .rdpsDistanceDiff(dist) // 2 decimal places
                    .rdpsKm(rdpsKm) // formatted
                    .gsmSignalStrength(deviceLocation.getGsmSignalStrength() != null
                            ? String.valueOf(deviceLocation.getGsmSignalStrength()) : "data_not_found")
                    .voltageLevel(deviceLocation.getVoltageLevel() != null
                            ? String.valueOf(deviceLocation.getVoltageLevel()) : "data_not_found")
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public String deleteAllLocations(Long imei, List<Long> timestamps, String deletedBy) {
        if (!divisionLoginRepository.existsById(deletedBy)) {
            throw new ResourceNotFoundException("User not found");
        }

        List<DeviceLocation> deviceLocations = deviceLocationRepository.findByDeviceImeiAndTimestampIn(imei,
                timestamps);
        if (deviceLocations == null) {
            throw new ResourceNotFoundException("Please refresh your data once");
        }

        log.info("Location Info {}", deviceLocations.stream().findFirst());

        locationOutOfIndiaService.createDeletedLocation(deviceLocations, deletedBy);
        long deletedCount = deviceLocationRepository.deleteByDeviceImeiAndTimestampIn(imei, timestamps);
        log.info("IMei {}", imei);
        log.info("Timestamps {}", timestamps);
        log.info("Division Id {}", deletedBy);
        return deletedCount + " locations deleted";
    }
}
