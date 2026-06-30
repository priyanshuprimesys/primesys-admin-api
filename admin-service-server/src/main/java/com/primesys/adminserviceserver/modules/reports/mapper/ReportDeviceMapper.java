package com.primesys.adminserviceserver.modules.reports.mapper;

import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DeviceReportConfigDTO;

public final class ReportDeviceMapper {

    public static DeviceReportConfigDTO toDeviceConfigDTO(DeviceEntity deviceEntity) {
        DeviceReportConfigDTO deviceReportConfigDTO = new DeviceReportConfigDTO();
        deviceReportConfigDTO.setDeviceNo(deviceEntity.getDeviceNo());
        deviceReportConfigDTO.setDeviceName(deviceEntity.getDeviceName());
        deviceReportConfigDTO.setDeviceType(deviceEntity.getDeviceTypeId());
        deviceReportConfigDTO.setReportEnable(deviceEntity.getReportEnable());
        deviceReportConfigDTO.setReportDistMargin(deviceEntity.getReportDistMargin());
        deviceReportConfigDTO.setReportTimeMargin(deviceEntity.getReportTimeMargin());
        deviceReportConfigDTO.setActiveStatus(deviceEntity.getActiveStatus());
        deviceReportConfigDTO.setShiftType(deviceEntity.getShiftType());
        return deviceReportConfigDTO;
    }
}
