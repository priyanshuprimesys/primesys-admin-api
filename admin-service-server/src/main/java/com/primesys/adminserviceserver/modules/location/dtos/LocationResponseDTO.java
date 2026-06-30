package com.primesys.adminserviceserver.modules.location.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LocationResponseDTO {
    String latDirection;
    String lonDirection;
    Double lat;
    Double lon;
    Integer speed;
    Long timestamp;
    String deviceUserType;
    String name;
    Long deviceImei;
}
