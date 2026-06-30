package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.primesys.adminservicemongodb.model.DeviceLocationStatus;
import com.primesys.adminservicemongodb.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
@Document(collection = "location_out_off_india")
public class LocationOutOfIndia {
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

    @Field("deleted_by")
    private String deletedBy;
}
