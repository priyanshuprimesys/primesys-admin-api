package com.primesys.adminserviceserver.dtos.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NearestRdpsDTO {

    private GeoLocationDTO geoLocation;
    private String featureDetail;
    private Integer kilometer;
    private Integer distance;
    private Double distanceDiff;
}