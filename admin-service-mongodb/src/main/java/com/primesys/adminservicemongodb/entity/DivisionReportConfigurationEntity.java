package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document("division_report_configuration")
public class DivisionReportConfigurationEntity {
    @MongoId
    Object id;

    @Field("track_division_id")
    private String trackDivisionId;

    @Field("path")
    private String path;

    /// report lock settings
    @Field("report_lock_time")
    private Long reportLockTime;

    @Field("report_halt_from_date")
    private Long reportHaltFromDate;

    @Field("report_halt_to_date")
    private Long reportHaltToDate;

    /// Overspeed settings
    @Field("overspeed_point_count")
    private Integer overSpeedPointCount;

    @Field("overspeed_distance_meter")
    private Double overspeedDistanceMeter;

    @Field("overspeed_remark")
    private String overspeedRemark;

    @Field("overspeed_remark_type")
    private String OverSpeedRemarkTypeEnum;

    /// Break Settings

    @Field("enable_break_time_check")
    private Boolean enableBreakTimeCheck;

    /// variables
    @Field("is_active")
    private Boolean isActive;

    /// Audit Fields
    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;

    @Field("created_at")
    private Long createdAt;

    @Field("updated_at")
    private Long updatedAt;
}
