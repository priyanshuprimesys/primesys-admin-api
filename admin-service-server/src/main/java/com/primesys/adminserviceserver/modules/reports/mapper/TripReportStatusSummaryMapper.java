package com.primesys.adminserviceserver.modules.reports.mapper;

import com.primesys.adminservicemongodb.entity.TripStatusReportSummaryEntity;
import com.primesys.adminserviceserver.modules.reports.dtos.TripStatusSummaryReportDTO;

public final class TripReportStatusSummaryMapper {
    public static TripStatusSummaryReportDTO toDTO(TripStatusReportSummaryEntity entity) {
        TripStatusSummaryReportDTO dto = new TripStatusSummaryReportDTO();
        dto.setPath(entity.getPath());
        dto.setName(entity.getName());
        dto.setDelayedStart(entity.getDelayedStart());
        dto.setDeviceOff(entity.getDeviceOff());
        dto.setOverSpeed(entity.getOverSpeed());
        dto.setTripCompleted(entity.getTripCompleted());
        dto.setReportOfTheDay(entity.getReportOfTheDay());
        dto.setTrackDivisionId(dto.getTrackDivisionId());
        if (entity.getShiftType() > 0) {
            dto.setShiftType(entity.getShiftType());
        }
        dto.setTripNotCompleted(entity.getTripNotCompleted());
        dto.setDeviceTypeId(entity.getDeviceTypeId());
        return dto;
    }
}
