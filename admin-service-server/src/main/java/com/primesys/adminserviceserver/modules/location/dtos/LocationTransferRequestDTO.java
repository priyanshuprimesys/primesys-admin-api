package com.primesys.adminserviceserver.modules.location.dtos;

import java.util.List;

public record LocationTransferRequestDTO(List<Long> imeiNos, String divisionId, String usedId, Long fromStartTime,
        Long fromEndTime, Long toStartTime, Long toEndTime) {
}
