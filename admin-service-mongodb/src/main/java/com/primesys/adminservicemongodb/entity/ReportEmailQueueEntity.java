package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.enums.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document("report_email_queue")
@CompoundIndex(name = "unique_report_queue", def = "{'division_id':1,'device_type_id':1,'report_end_time':1}", unique = true)
public class ReportEmailQueueEntity {

    @MongoId
    private String id;

    @Field("division_id")
    private String divisionId;

    @Field("device_type_id")
    private Integer deviceTypeId;

    @Field("report_date")
    private Long reportDate;

    @Field("report_end_time")
    private Long reportEndTime;

    @Field("report_extended_time")
    private Long reportExtendedEndTime;

    @Field("sent")
    private Boolean sent;

    @Field("sent_at")
    private Long sentAt;

    @Indexed
    @Field("status")
    private EmailStatus status = EmailStatus.PENDING;

    @Field("processing_started_at")
    private Long processingStartedAt;

    @Field("retry_count")
    private Integer retryCount = 0;

    @Field("error_message")
    private String errorMessage;

    @Field("error_at")
    private Long errorAt;

    @Field("triggered_by")
    private String triggeredBy;

    @Field("created_at")
    private Long createdAt;

    @Field("updated_at")
    private Long updatedAt;
}
