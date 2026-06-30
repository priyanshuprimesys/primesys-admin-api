package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.enums.EmailStatus;
import com.primesys.adminservicemongodb.enums.ProcessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document("report_email_queue_log")
@CompoundIndex(name = "idx_log_queue", def = "{'queue_id':1,'status':1}")
public class ReportEmailQueueLogEntity {

    @MongoId
    private String id;

    /** Links this log entry back to the parent report_email_queue document. */
    @Field("queue_id")
    private String queueId;

    /** Root division from the queue entry (the one that triggered the run). */
    @Field("division_id")
    private String divisionId;

    @Field("device_type_id")
    private Integer deviceTypeId;

    @Field("report_date")
    private Long reportDate;

    /** Which tree-node division this log entry is about. */
    @Field("process_division_id")
    private String processDivisionId;

    /** Human-readable name of the division being processed. */
    @Field("process_division_name")
    private String processDivisionName;

    /** Whether this entry tracks sendReportEmail or sendParentEmail. */
    @Field("process_type")
    private ProcessType processType;

    /** Comma-separated list of email addresses the email was sent to. */
    @Field("email_sent_to")
    private String emailSentTo;

    /** SENDING while in-progress, SENT on success, FAILED on error. */
    @Field("status")
    private EmailStatus status;

    @Field("triggered_by")
    private String triggeredBy;

    @Field("error_message")
    private String errorMessage;

    @Field("processing_started_at")
    private Long processingStartedAt;

    @Field("created_at")
    private Long createdAt;

    @Field("updated_at")
    private Long updatedAt;
}