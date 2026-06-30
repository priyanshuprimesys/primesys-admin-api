package com.primesys.adminserviceserver.dtos.location;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDTO {

    private Integer gpsRealTime;
    private Integer gpsPosition;
    private String lonDirection;
    private String latDirection;
    private Integer course;
}
