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
public class DeviceCommandHistoryDto {
    Integer commandId;

    String deviceName;

    Long deviceImei;

    String command;

    String commandDeliveredMsg;

    String deviceCommandResponse;

    Long timestamp;
    Long deviceResponseTime;
    boolean isResend;
    Long resentAt;

    String loginName;
}
