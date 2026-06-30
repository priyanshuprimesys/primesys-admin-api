package com.primesys.adminserviceserver.modules.location.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationHistoryDTO {
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
