package com.primesys.adminservicemongodb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripStatus {
    @Field("in_out_status")
    @JsonProperty("in_out_status")
    private String inOutStatus;
    @Field("timestamp")
    @JsonProperty("timestamp")
    private Long timestamp;
    @Field("allocated_lc")
    @JsonProperty("allocated_lc")
    private String allocatedLc;
    private String location;
    private Integer distance;
    @Field("time_deviation")
    @JsonProperty("time_deviation")
    private Integer timeDeviation;
}
