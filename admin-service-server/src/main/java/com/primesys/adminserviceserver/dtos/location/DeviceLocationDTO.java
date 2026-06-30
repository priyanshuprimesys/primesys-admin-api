package com.primesys.adminserviceserver.dtos.location;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
@Builder
@Getter
public class DeviceLocationDTO {
    String latDirection;
    String lonDirection;
    Double lat;
    Double lon;
    Integer speed;
    Long timestamp;
    Boolean isBlind;
    Long blindLocationGetTimestamp;
    String featureDetail;
    Double rdpsDistanceDiff;
    String voltageLevel;
    String rdpsKm;

    String gsmSignalStrength;
}
