package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.enums.JobOrderExecutionStatus;
import com.primesys.adminservicemongodb.model.JobOrderDevice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;

@Document("job_order_execution_logs")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JobOrderExecutionLogEntity {
    @MongoId
    ObjectId id;

    @Field("job_order_id")
    private ObjectId jobOrderId;

    @Field("job_start_at")
    private Instant jobStartAt;

    @Field("job_order_type_id")
    private ObjectId jobOrderTypeId;

    @Field("job_end_at")
    private Instant jobEndAt;

    @Field("failed_devices_jobs")
    private List<JobOrderDevice> failedDevices;

    @Field("remark")
    private String remark;

    @Field("track_division_id")
    private String trackDivisionId;

    @Field("status")
    private JobOrderExecutionStatus status;

    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;
}
