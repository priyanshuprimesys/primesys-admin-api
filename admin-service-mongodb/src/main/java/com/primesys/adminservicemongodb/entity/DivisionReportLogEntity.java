package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.enums.StatusEnum;
import com.primesys.adminservicemongodb.model.DivisionTripReport;
import com.primesys.adminservicemongodb.model.ReportEmailLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("division_report_log")
public class DivisionReportLogEntity {
    @MongoId
    private ObjectId id;

    @Field("division_id")
    private String divisionId;

    @Field("track_division_id")
    private String trackDivisionId;

    @Field("device_type_id")
    private Integer deviceTypeId;

    @Field("generated_at")
    private Long generatedAt;

    @Field("path")
    private String path;

    @Field("report_email")
    ReportEmailLog reportEmail;

    /// the maximum end time a trip has to have means if 10 devices have 05 - 09 and another 4 has 10 hrs - 17 hrs then
    /// 17 is max time
    @Field("division_trip_max_time")
    private Long tripMaxTime;

    /// will save in seconds like 120 will convert to 2 hours
    @Field("report_lock_time")
    private Long tripLockTime;

    @Field("report_date")
    private Long reportDate;

    @Field("status")
    StatusEnum status;

    @Field("reports")
    private List<DivisionTripReport> reports;

}
