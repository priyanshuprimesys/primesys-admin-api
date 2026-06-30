package com.primesys.adminserviceserver.dtos.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoLocationDTO {

    private String type;
    private double[] coordinates;
}
