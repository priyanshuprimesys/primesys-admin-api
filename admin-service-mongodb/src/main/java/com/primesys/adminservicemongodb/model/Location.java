package com.primesys.adminservicemongodb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
public class Location {
    @Id
    String id;
    @Field("device_imei")
    Long deviceImei;
    Long timestamp;
    @Field("geo_location")
    GeoLocation geoLocation;
    Integer speed;
    @Field("satellite_no")
    Integer satelliteNo;
    DeviceLocationStatus status;
    @Field("blind")
    Boolean blind;
    @Field("blind_received_at")
    Long blindReceivedAt;

    @Field("nearest_rdps")
    private Object nearestRdps;

    @Field("voltage_level")
    private Object voltageLevel;

    @Field("gsm_signal_strength")
    private Object gsmSignalStrength;
}
