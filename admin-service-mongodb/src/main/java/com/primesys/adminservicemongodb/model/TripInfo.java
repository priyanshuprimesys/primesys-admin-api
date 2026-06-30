package com.primesys.adminservicemongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
public class TripInfo {

    @Field("start_time")
    private Long startTime;

    @Field("end_time")
    private Long endTime;

    @Field("break_start_time")
    private Long breakStartTime;

    @Field("break_end_time")
    private Long breakEndTime;

    @Field("trip_start_km")
    private Double tripStartKm;

    @Field("trip_end_km")
    private Double tripEndKm;

    @Field("section_name")
    private String sectionName;

    @Field("approved_status")
    private Boolean approvedStatus;

    @Field("created_at")
    private Long createdAt;

    @Field("device_type_id")
    private Integer deviceTypeId;

    @Field("trip_no")
    private Integer tripNo;

    @Field("created_by")
    private String createdBy;
}
