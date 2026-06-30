package com.primesys.adminservicecommon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceExchangeDTO {
    String id;
    Long oldDeviceIMEI;
    String oldDeviceName;
    String oldDeviceSimNo;
    String oldDeviceSimIMEINo;
    Integer oldDeviceNo;
    Integer oldDeviceTypeId;
    String divisionId;
    String exchangeBy;
    Long exchangeAt;
    Long newDeviceIMEI;
    String newDeviceName;
    String newDeviceSimNo;
    String newDeviceSimIMEINo;
    Integer newDeviceNo;
    Integer newDeviceTypeId;
}
