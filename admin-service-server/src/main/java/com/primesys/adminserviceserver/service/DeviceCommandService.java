package com.primesys.adminserviceserver.service;

import com.primesys.adminservicecommon.dto.DeviceCommandDto;
import com.primesys.adminservicecommon.dto.DeviceCommandHistoryDto;
import com.primesys.adminservicemongodb.entity.DeviceCommandHistoryEntity;

import java.util.List;

public interface DeviceCommandService {

    List<DeviceCommandDto> getAllDeviceCommands();

    List<DeviceCommandHistoryDto> getAllDeviceCommandHistoryForDate(long startTime, long endTime);

    List<DeviceCommandHistoryEntity> sendCommand(List<DeviceCommandHistoryEntity> deviceCommandHistoryEntity);

    List<DeviceCommandHistoryEntity> sendCommandEmergency(List<DeviceCommandHistoryEntity> command);
}
