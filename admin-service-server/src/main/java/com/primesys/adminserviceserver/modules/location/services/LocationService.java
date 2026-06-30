package com.primesys.adminserviceserver.modules.location.services;

import com.primesys.adminserviceserver.modules.location.dtos.LocationHistoryDTO;

import java.util.List;

public interface LocationService {
    List<LocationHistoryDTO> getDeviceLocationHistory(Long imeiNo, Long startTime, Long endTime);

    String transferBulkLocationBackup(List<Long> imeiNos, Long startTime, Long endTime);

    String copyLocationFromDateToDate(List<Long> imeiNos, Long fromStartTime, Long fromEndTime, Long toStartTime,
            Long toEndTime);

    String revertLocationCopyProcess(List<Long> imeiNos, String divisionId, String userId, Long fromStartTime,
            Long fromEndTime, Long toStartTime, Long toEndTime);

    String destroyLocationProcess(List<Long> imeiNos, String divisionId, String userId, Long fromStartTime,
            Long fromEndTime, Long toStartTime, Long toEndTime);

}
