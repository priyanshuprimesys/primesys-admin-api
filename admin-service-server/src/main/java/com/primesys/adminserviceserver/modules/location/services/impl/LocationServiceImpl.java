package com.primesys.adminserviceserver.modules.location.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primesys.adminservicemongodb.entity.DeviceHistoryEntity;
import com.primesys.adminservicemongodb.entity.LocationBackupEntity;
import com.primesys.adminservicemongodb.entity.LocationTransferLog;
import com.primesys.adminservicemongodb.model.NearestRdps;
import com.primesys.adminservicemongodb.repository.DeviceHistoryRepository;
import com.primesys.adminservicemongodb.repository.LocationBackupRepository;
import com.primesys.adminservicemongodb.repository.LocationTransferLogRepository;
import com.primesys.adminserviceserver.modules.location.dtos.LocationHistoryDTO;
import com.primesys.adminserviceserver.modules.location.mapper.LocationBackupMapper;
import com.primesys.adminserviceserver.modules.location.services.LocationService;
import com.primesys.adminserviceserver.utility.DateTimeUtility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.DateUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private static final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);
    private final DeviceHistoryRepository deviceHistoryRepository;
    private final LocationBackupRepository locationBackupRepository;
    private final LocationTransferLogRepository locationTransferLogRepository;

    @Override
    public String transferBulkLocationBackup(List<Long> imeiNos, Long startTime, Long endTime) {

        if (imeiNos.isEmpty()) {
            return "No devices found";
        }

        List<DeviceHistoryEntity> locationData = deviceHistoryRepository.findAllByDeviceImeisAndTimestampRange(imeiNos,
                startTime, endTime);

        if (!locationData.isEmpty()) {
            List<LocationBackupEntity> backupList = locationData.stream().map(LocationBackupMapper::toEntity).toList();

            List<LocationBackupEntity> backUpData = locationBackupRepository
                    .findAllByDeviceImeisAndTimestampRange(imeiNos, startTime, endTime);
            locationBackupRepository.saveAll(backupList);

            if (backUpData.size() == locationData.size()) {
                deviceHistoryRepository.deleteAll(locationData);
            }

            return backupList.size() + " records transferred successfully and locations deleted successfully";
        }
        return "Successfully done";
    }

    @Override
    public String copyLocationFromDateToDate(List<Long> imeiNos, Long fromStartTime, Long fromEndTime, Long toStartTime,
            Long toEndTime) {

        if (fromStartTime > toStartTime) {
            return "Location copy failed as you have selected wrong date, Start time has to be less than End Time";
        }

        Long secondsDiff = toStartTime - fromStartTime;

        log.info("diff {}", secondsDiff);

        List<DeviceHistoryEntity> data = deviceHistoryRepository.findAllByDeviceImeisAndTimestampRange(imeiNos,
                fromStartTime, fromEndTime);

        for (DeviceHistoryEntity deviceHistoryEntity : data) {
            log.info("{}location saving", deviceHistoryEntity.getDeviceImei());
            DeviceHistoryEntity deviceHistory = LocationBackupMapper.toEntityWithSecondsDiff(deviceHistoryEntity,
                    secondsDiff);
            deviceHistoryRepository.save(deviceHistory);
        }

        return "All Locations has been transferred from " + DateTimeUtility.epochToDate(fromStartTime) + " to "
                + DateTimeUtility.epochToDate(toEndTime);
    }

    @Override
    public String revertLocationCopyProcess(List<Long> imeiNos, String divisionId, String userId, Long fromStartTime,
            Long fromEndTime, Long toStartTime, Long toEndTime) {

        List<LocationBackupEntity> locationBackupEntities = locationBackupRepository
                .findAllByDeviceImeisAndTimestampRange(imeiNos, toStartTime, toEndTime);
        if (locationBackupEntities.isEmpty()) {
            return "No locations saved";
        }

        List<DeviceHistoryEntity> deviceHistoryEntities = locationBackupEntities.stream()
                .map(LocationBackupMapper::toDeviceEntity).toList();

        deviceHistoryRepository.saveAll(deviceHistoryEntities);
        LocationTransferLog locationTransferLog = createLog(imeiNos, divisionId, userId, fromStartTime, fromEndTime,
                toStartTime, toEndTime, "Locations have been successfully recovered");

        locationTransferLogRepository.save(locationTransferLog);
        return "Locations have been successfully recovered";
    }

    @Override
    public String destroyLocationProcess(List<Long> imeiNos, String divisionId, String userId, Long fromStartTime,
            Long fromEndTime, Long toStartTime, Long toEndTime) {
        List<DeviceHistoryEntity> locationData = deviceHistoryRepository.findAllByDeviceImeisAndTimestampRange(imeiNos,
                toStartTime, toEndTime);

        if (locationData.isEmpty()) {
            return "Locations are empty";
        }

        LocationTransferLog locationTransferLog = createLog(imeiNos, divisionId, userId, fromStartTime, fromEndTime,
                toStartTime, toEndTime, "Locations have been successfully deleted");

        deviceHistoryRepository.deleteAll(locationData);
        locationTransferLogRepository.save(locationTransferLog);
        return "Locations have been deleted";
    }

    @Override
    public List<LocationHistoryDTO> getDeviceLocationHistory(Long imeiNo, Long startTime, Long endTime) {

        List<DeviceHistoryEntity> deviceHistoryEntities = deviceHistoryRepository
                .findByDeviceImeiAndTimestampBetween(imeiNo, startTime, endTime);

        if (deviceHistoryEntities.isEmpty()) {
            return List.of();
        }

        return deviceHistoryEntities.stream().map(dHEntity -> {

            String featureDetail = "data_not_found";
            double dist = 0;
            String rdpsKm = "";

            if (dHEntity.getNearestRdps() != null && !dHEntity.getNearestRdps().toString().equals("data_not_found")) {

                ObjectMapper mapper = new ObjectMapper();
                NearestRdps nearestRdps = mapper.convertValue(dHEntity.getNearestRdps(), NearestRdps.class);

                featureDetail = nearestRdps.getFeatureDetail();

                // Format to 2 decimal places
                DecimalFormat df = new DecimalFormat("#.00");

                double kmValue = nearestRdps.getKilometer();
                double kmPlusDistance = kmValue + (nearestRdps.getDistance() / 1000.0);

                rdpsKm = kmValue + "/" + nearestRdps.getDistance() + " [" + df.format(kmPlusDistance) + "]";

                // Round dist also to 2 decimal places
                dist = Math.round(nearestRdps.getDistanceDiff() * 100.0) / 100.0;
            }

            return LocationHistoryDTO.builder().timestamp(dHEntity.getTimestamp()).speed(dHEntity.getSpeed())
                    .isBlind(dHEntity.getBlind()).lon(dHEntity.getGeoLocation().getCoordinates().get(0))
                    .lat(dHEntity.getGeoLocation().getCoordinates().get(1))
                    .blindLocationGetTimestamp(dHEntity.getBlindReceivedAt())
                    .lonDirection(dHEntity.getStatus().getLonDirection())
                    .latDirection(dHEntity.getStatus().getLatDirection()).featureDetail(featureDetail)
                    .rdpsDistanceDiff(dist) // 2 decimal places
                    .rdpsKm(rdpsKm) // formatted
                    .gsmSignalStrength(dHEntity.getGsmSignalStrength() != null
                            ? String.valueOf(dHEntity.getGsmSignalStrength()) : "data_not_found")
                    .voltageLevel(dHEntity.getVoltageLevel() != null ? String.valueOf(dHEntity.getVoltageLevel())
                            : "data_not_found")
                    .build();

        }).collect(Collectors.toList());
    }

    private LocationTransferLog createLog(List<Long> imeiNos, String divisionId, String userId, Long fromStartTime,
            Long fromEndTime, Long toStartTime, Long toEndTime, String message) {
        LocationTransferLog locationTransferLog = new LocationTransferLog();

        locationTransferLog.setDivisionId(divisionId);
        locationTransferLog.setCreatedBy(userId);
        locationTransferLog.setCreatedAt(DateTimeUtility.toMidNightEpoch());
        locationTransferLog.setFromStartDate(fromStartTime);
        locationTransferLog.setFromEndDate(fromEndTime);
        locationTransferLog.setToStartDate(toStartTime);
        locationTransferLog.setToEndDate(toEndTime);
        locationTransferLog.setMessage(message);
        locationTransferLog.setImeiNos(imeiNos);
        return locationTransferLog;
    }
}
