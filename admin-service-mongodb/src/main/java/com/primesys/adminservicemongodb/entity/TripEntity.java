package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("trip_data")
public class TripEntity {
    @Id
    String id;
    @Field("start_time")
    Long startTime;
    @Field("end_time")
    Long endTime;
    @Field("break_start_time")
    Long breakStartTime;
    @Field("break_end_time")
    Long breakEndTime;
    @Field("trip_start_km")
    Double tripStartKm;
    @Field("trip_end_km")
    Double tripEndKm;
    @Field("device_name")
    String deviceName;
    @Field("section_name")
    String sectionName;
    @Field("active_status")
    Boolean activeStatus;
    @Field("trip_no")
    Integer tripNo;
    @Field("approved_status")
    Boolean approvedStatus;
    @Field("student_id")
    Integer studentId;
    @Field("device_imei")
    Long deviceImei;
    Long version;
    @Field("created_at")
    Date createdAt;
    @Field("last_modified")
    Date updatedAt;

}
