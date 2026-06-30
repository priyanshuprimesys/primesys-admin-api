package com.primesys.adminservicemongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceLocationStatus {
    @Field("gps_real_time")
    Integer gpsRealTime;
    @Field("gps_position")
    Integer gpsPosition;
    @Field("lon_direction")
    String lonDirection;
    @Field("lat_direction")
    String latDirection;
    Integer cource;
}
