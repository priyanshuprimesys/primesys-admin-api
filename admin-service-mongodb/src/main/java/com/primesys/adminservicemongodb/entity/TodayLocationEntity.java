package com.primesys.adminservicemongodb.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@Document(collection = "todays_location") // Replace with actual collection name
public class TodayLocationEntity {

    @Id
    private String id;

    @Field("device_imei")
    private Long deviceImei;

    @Field("geo_location")
    private GeoLocation geoLocation;

    private Integer speed;

    private Long timestamp;

    private Status status;

    @Field("satellite_no")
    private Integer satelliteNo;

    @Field("nearest_rdps")
    private Object nearestRdps;

    @Field("voltage_level")
    private Object voltageLevel;

    @Field("gsm_signal_strength")
    private Object gsmSignalStrength;

    // Inner static classes for embedded fields

    @Data
    public static class GeoLocation {
        private String type;
        private double[] coordinates;
    }

    @Data
    public static class Status {
        @Field("gps_real_time")
        private Integer gpsRealTime;

        @Field("gps_position")
        private Integer gpsPosition;

        @Field("lon_direction")
        private String lonDirection;

        @Field("lat_direction")
        private String latDirection;

        @Field("cource")
        private Integer course;
    }

    @Data
    public static class NearestRdps {
        @Field("geo_location")
        private GeoLocation geoLocation;

        @Field("feature_detail")
        private String featureDetail;

        private Integer kilometer;
        private Integer distance;

        @Field("distance_diff")
        private Double distanceDiff;
    }
}
