package com.primesys.adminserviceserver.modules.location.dtos;

import java.util.List;

public record LocationBulkBackupRequestDTO(List<Long> imeiNos, String divisionId, Integer deviceTypeId, Long startTime,
        Long endTime) {
}