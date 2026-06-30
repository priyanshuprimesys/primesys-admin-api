package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document("location_transfer_log")
public class LocationTransferLog {
    @MongoId
    ObjectId id;

    @Field("division_id")
    String divisionId;

    @Field("message")
    String message;

    @Field("imei_nos")
    List<Long> imeiNos;

    @Field("from_start_date")
    Long fromStartDate;

    @Field("from_end_date")
    Long fromEndDate;

    @Field("to_start_date")
    Long toStartDate;

    @Field("to_end_date")
    Long toEndDate;

    @Field("created_by")
    String createdBy;

    @Field("created_at")
    Long createdAt;
}
