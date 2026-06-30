package com.primesys.adminserviceserver.modules.reports.services.impl;

import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.repository.DeviceRepository;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DeviceConfigStatusDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DeviceReportConfigDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DivisionDeviceReportVariableDTO;
import com.primesys.adminserviceserver.modules.reports.mapper.ReportDeviceMapper;
import com.primesys.adminserviceserver.modules.reports.services.ReportConfigurationService;
import com.primesys.adminserviceserver.utility.DateTimeUtility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportConfigurationServiceImpl implements ReportConfigurationService {

    private final DeviceRepository deviceRepository;
    private final DivisionLoginRepository divisionLoginRepository;

    @Override
    public List<DivisionDeviceReportVariableDTO> getDevicesReportConfig(String divisionId, Integer deviceTypeId) {

        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository
                .findByPathContainingWithDeviceList(divisionId);

        /// list of devices with report config variables
        List<DivisionDeviceReportVariableDTO> deviceReportVariableDTOS = new ArrayList<>();

        for (DivisionLoginEntity divisionLoginEntity : divisionLoginEntities) {
            List<Integer> deviceNos = Arrays.stream(divisionLoginEntity.getDeviceList().split(",")).map(String::trim)
                    .filter(s -> !s.isBlank()).map(Integer::parseInt).toList();
            List<DeviceEntity> deviceEntities = deviceRepository.findDevicesByDivisionAndDeviceType(
                    divisionLoginEntity.getTrackDivisionId(), deviceTypeId, deviceNos);
            DivisionDeviceReportVariableDTO dto = new DivisionDeviceReportVariableDTO();
            List<DeviceReportConfigDTO> deviceReportConfigDTOS1 = deviceEntities.stream()
                    .map(ReportDeviceMapper::toDeviceConfigDTO).toList();
            dto.setDivisionName(divisionLoginEntity.getName());
            dto.setReportEnable(divisionLoginEntity.getReportEnable());
            dto.setActiveStatus(divisionLoginEntity.getActiveStatus());
            dto.setDevices(deviceReportConfigDTOS1);
            deviceReportVariableDTOS.add(dto);
        }
        return deviceReportVariableDTOS;
    }

    @Override
    public String updateDeviceAndReportStatus(String divisionId, List<DeviceConfigStatusDTO> deviceConfigs,
            Integer deviceTypeId) {
        DivisionLoginEntity divisionLogin = divisionLoginRepository.findById(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("No such division exists"));

        int updatedCount = 0;

        for (DeviceConfigStatusDTO config : deviceConfigs) {

            List<Integer> deviceNos = Arrays.stream(config.devices().split(",")).map(String::trim)
                    .filter(s -> !s.isBlank()).map(Integer::parseInt).toList();

            List<DeviceEntity> deviceEntities = deviceRepository
                    .findDevicesByDivisionAndDeviceType(divisionLogin.getTrackDivisionId(), deviceTypeId, deviceNos);

            deviceEntities.forEach(device -> {

                if (config.activeStatus() != null) {
                    device.setActiveStatus(config.activeStatus());
                }

                if (config.reportEnable() != null) {
                    if (config.activeStatus() != null && !config.activeStatus()) {
                        device.setReportEnable(false);
                    } else {
                        device.setReportEnable(config.reportEnable());
                    }
                }
                device.setActivationDate(DateTimeUtility.currentEpoch());
            });

            updatedCount += deviceEntities.size();

            deviceRepository.saveAll(deviceEntities);
        }

        return updatedCount + " Device(s) Updated Successfully";
    }

    @Override
    public String updateDivisionDeviceAndReportStatus(String divisionId, Boolean reportEnable, Boolean activeStatus) {

        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository
                .findByPathContainingWithDeviceList(divisionId);

        int updatedCount = 0;

        for (DivisionLoginEntity divisionLoginEntity : divisionLoginEntities) {

            divisionLoginEntity.setReportEnable(reportEnable);

            List<Integer> deviceNos = Arrays.stream(divisionLoginEntity.getDeviceList().split(",")).map(String::trim)
                    .filter(s -> !s.isBlank()).map(Integer::parseInt).toList();

            List<DeviceEntity> deviceEntities = deviceRepository
                    .findDevicesByDivisionAndDeviceNos(divisionLoginEntity.getTrackDivisionId(), deviceNos);

            if (Boolean.FALSE.equals(activeStatus)) {
                deviceEntities.forEach(device -> {
                    device.setActiveStatus(false);
                    device.setReportEnable(false);
                });
            } else {
                deviceEntities.forEach(device -> {
                    device.setActiveStatus(true);

                    if (reportEnable != null) {
                        device.setReportEnable(reportEnable);
                    }
                });
            }

            deviceRepository.saveAll(deviceEntities);
            divisionLoginRepository.save(divisionLoginEntity);
        }

        return updatedCount + " Device(s) Updated Successfully";
    }

    @Override
    public String divisionDeviceAndReportActiveStatus(String divisionId, Boolean divisionStatus) {
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository
                .findByPathContainingWithDeviceList(divisionId);
        int updatedCount = 0;
        for (DivisionLoginEntity divisionLoginEntity : divisionLoginEntities) {

            divisionLoginEntity.setReportEnable(divisionStatus);
            divisionLoginEntity.setActiveStatus(divisionStatus);

            List<Integer> deviceNos = Arrays.stream(divisionLoginEntity.getDeviceList().split(",")).map(String::trim)
                    .filter(s -> !s.isBlank()).map(Integer::parseInt).toList();

            List<DeviceEntity> deviceEntities = deviceRepository
                    .findDevicesByDivisionAndDeviceNos(divisionLoginEntity.getTrackDivisionId(), deviceNos);

            deviceEntities.forEach(device -> {
                device.setActiveStatus(divisionStatus);
                device.setReportEnable(divisionStatus);
            });

            deviceRepository.saveAll(deviceEntities);
            divisionLoginRepository.save(divisionLoginEntity);
            updatedCount++;
        }

        return updatedCount + " Device(s) Updated Successfully";
    }
}
