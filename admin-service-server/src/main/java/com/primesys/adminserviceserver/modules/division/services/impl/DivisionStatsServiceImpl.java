package com.primesys.adminserviceserver.modules.division.services.impl;

import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.repository.DeviceRepository;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminserviceserver.modules.division.dtos.DivisionStatsDTO;
import com.primesys.adminserviceserver.modules.division.services.DivisionStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DivisionStatsServiceImpl implements DivisionStatsService {

    private final DivisionLoginRepository divisionLoginRepository;
    private final DeviceRepository deviceRepository;

    @Override
    public DivisionStatsDTO getDivisionStats() {
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository.findAll();
        List<DeviceEntity> deviceEntities = deviceRepository.findAll();
        Integer totalRailwayUser = (int) divisionLoginEntities.stream()
                .filter(d -> Boolean.TRUE.equals(d.getIsRailwayUser())).count();
        Integer totalActiveDivision = (int) divisionLoginEntities.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActiveStatus())).count();

        Integer totalInActiveDivision = (int) divisionLoginEntities.stream()
                .filter(d -> Boolean.FALSE.equals(d.getActiveStatus())).count();

        Integer totalActiveDevice = (int) deviceEntities.stream().filter(d -> Boolean.TRUE.equals(d.getActiveStatus()))
                .count();

        Integer totalInActiveDevice = (int) deviceEntities.stream()
                .filter(d -> Boolean.FALSE.equals(d.getActiveStatus())).count();
        Integer totalDivisionCount = divisionLoginEntities.stream()
                .filter(divisionLoginEntity -> divisionLoginEntity.getRoleId() == 7).toList().size();
        Integer totalDevices = deviceEntities.size();
        DivisionStatsDTO dto = new DivisionStatsDTO();
        dto.setTotalDivision(totalDivisionCount);
        dto.setActiveDivision(totalActiveDivision);
        dto.setInactiveDivision(totalInActiveDivision);
        dto.setTotalDevices(totalDevices);
        dto.setTotalInActiveDevices(totalInActiveDevice);
        dto.setTotalActiveDevices(totalActiveDevice);
        dto.setTotalRailwayUser(totalRailwayUser);
        return dto;
    }
}
