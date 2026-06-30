package com.primesys.adminservicemongodb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NearestRdps {
    @JsonProperty("geo_location")

    @Field("geo_location")
    private GeoLocation geoLocation;
    @JsonProperty("feature_detail")

    @Field("feature_detail")
    private String featureDetail;

    private int kilometer;
    private int distance;
    @JsonProperty("rdps_id")

    @Field("rdps_id")
    private String rdpsId;
    @JsonProperty("distance_diff")

    @Field("distance_diff")
    private double distanceDiff;

    // Getters and Setters
}
