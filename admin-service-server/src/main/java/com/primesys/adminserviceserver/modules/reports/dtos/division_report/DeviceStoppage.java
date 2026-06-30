package com.primesys.adminserviceserver.modules.reports.dtos.division_report;

import com.primesys.adminservicemongodb.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceStoppage {
    @Field("stoppage_rdps_km")
    Integer stoppageRdpsKm;
    @Field("stoppage_rdps_distance")
    Double stoppageRdpsDistance;
    @Field("stoppage_rdps_details")
    String stoppageRdpsDetails;
    @Field("stoppage_time")
    Integer stoppageTime;
    @Field("stoppage_start_time")
    Integer stoppageStartTime;
    @Field("geo_location")
    GeoLocation geoLocation;

}