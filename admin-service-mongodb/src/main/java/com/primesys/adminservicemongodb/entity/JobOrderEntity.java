package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.enums.JobOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.List;

@Document("job_orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobOrderEntity {
    @MongoId
    ObjectId id;

    @Field("job_name")
    private String jobName;

    @Field("status")
    private JobOrderStatus status;

    @Field("track_division_ids")
    private List<String> trackDivisionIds;

    @Field("type_id")
    private String typeId;

    @Field("end_at")
    private Instant endAt;

    @Field("start_from")
    private Instant startFrom;

    @Field("hold_upto")
    private Instant holdUpto;

    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;

    @Field("created_at")
    private Instant createdAt;

    @Field("updated_at")
    private Instant updatedAt;

    @Field("deleted_at")
    private Instant deletedAt;
}
