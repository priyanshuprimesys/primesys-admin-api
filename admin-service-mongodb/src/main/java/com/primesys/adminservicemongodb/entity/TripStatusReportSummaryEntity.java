package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "trip_status_report_summary")
public class TripStatusReportSummaryEntity {

    @MongoId
    private ObjectId id;

    @Field("name")
    private String name;
    @Field("path")
    private String path;

    @Field("device_off")
    private String deviceOff;

    @Field("trip_completed")
    private String tripCompleted;

    @Field("trip_not_completed")
    private String tripNotCompleted;

    @Field("off_track")
    private String offTrack;

    @Field("over_speed")
    private String overSpeed;
    @Field("delayed_start")
    private String delayedStart;
    @Field("track_division_id")
    private String trackDivisionId;

    @Field("report_of_the_day")
    private long reportOfTheDay;

    @Field("device_type_id")
    private int deviceTypeId;

    @Field("shift_type")
    private int shiftType;
}
