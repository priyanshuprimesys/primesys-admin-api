package com.primesys.adminserviceserver.modules.reports.mapper;

import com.primesys.adminservicemongodb.entity.DivisionReportLogEntity;
import com.primesys.adminserviceserver.modules.reports.dtos.DivisionReportLogDTO;

public final class ReportLogMapper {

    public static DivisionReportLogDTO toDto(DivisionReportLogEntity entity) {

        if (entity == null) {
            return null;
        }

        return new DivisionReportLogDTO(entity.getId().toString(), entity.getDivisionId(), entity.getDeviceTypeId(),
                entity.getGeneratedAt(), entity.getTripMaxTime(), entity.getTripLockTime(), entity.getReportDate(),
                entity.getStatus(), entity.getReports());
    }
}
