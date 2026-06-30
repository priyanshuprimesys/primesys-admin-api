package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("trip_data")
public class BeatEntity {
    @MongoId
    ObjectId id;
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
    @Field("section_name")
    String sectionName;
    @Field("season_id")
    String seasonId;
    @Field("active_status")
    Boolean activeStatus;
    @Field("approved_status")
    Boolean approvedStatus;
    @Field("approved_by")
    String approvedBy;
    @Deprecated
    @Field("student_id")
    Integer studentId;
    @Field("device_imei")
    Long deviceImei;
    @Field("device_no")
    int deviceNo;
    Long version;
    @Field("created_at")
    Long createdAt;
    @Field("updated_at")
    Long updatedAt;
    @Field("shift_type")
    Integer shiftType;
    @Field("device_type_id")
    Integer deviceTypeId;
    @Field("trip_no")
    Integer tripNo;
    @Field("device_name")
    String device_name;
    @Field("ref_file_name")
    String refFileName;
    @Field("created_by")
    String createdBy;
    @Field("updated_by")
    String updatedBy;
    @Field("t_start_time")
    Long tStartTime;
    @Field("t_end_time")
    Long tEndTime;
    @Field("trip_time")
    Long tripTime;
    @Field("t_shift_type")
    int tShiftType;

    @Field("schedule_id")
    String scheduleId;
    @Field("schedule_date")
    LocalDate scheduleDate;
    @Field("direction")
    String direction;
    @Field("ref_file_id")
    String refFileId;
    @Field("is_hourly")
    Boolean isHourly;
    @Field("trip_count")
    Double tripCount;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
