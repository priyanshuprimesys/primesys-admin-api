package com.primesys.adminserviceserver.modules.location.workflow;

import com.primesys.adminservicemongodb.entity.LocationTransferLog;
import com.primesys.adminservicemongodb.repository.LocationTransferLogRepository;
import com.primesys.adminserviceserver.modules.location.services.LocationService;
import com.primesys.adminserviceserver.utility.DateTimeUtility;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationWorkflow {

    private static final Logger log = LoggerFactory.getLogger(LocationWorkflow.class);
    private final LocationService locationService;
    private final LocationTransferLogRepository locationTransferLogRepository;

    /// This is the transfer workflow
    public String transferLocationWithBackup(List<Long> imeiNos, String divisionId, String userId, Long fromStartTime,
            Long fromEndTime, Long toStartTime, Long toEndTime) {

        /// backup has been taken of the date to which location need to be refreshed
        /// means if 7 locations have to be copied to 10 then 10 is date of backup
        String message = locationService.transferBulkLocationBackup(imeiNos, toStartTime, toEndTime);
        LocationTransferLog locationTransferLog = createLog(imeiNos, divisionId, userId, fromStartTime, fromEndTime,
                toStartTime, toEndTime, message);
        locationTransferLogRepository.save(locationTransferLog);

        if (!message.toLowerCase().contains("successfully".toLowerCase())) {
            return message;
        } else {
            String copyMessage = locationService.copyLocationFromDateToDate(imeiNos, fromStartTime, fromEndTime,
                    toStartTime, toEndTime);
            LocationTransferLog locationTransferLog1 = createLog(imeiNos, divisionId, userId, fromStartTime,
                    fromEndTime, toStartTime, toEndTime, copyMessage);
            locationTransferLogRepository.save(locationTransferLog1);
            return copyMessage;
        }

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
