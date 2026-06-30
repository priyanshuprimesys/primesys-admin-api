package com.primesys.adminservicemongodb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DevicePacketDto {
    private String id;
    private Long deviceImei;
    private String packet;
    private Long timestamp;
    private String packetFrom;
    private String packetType;
    private String asciiData;
}
