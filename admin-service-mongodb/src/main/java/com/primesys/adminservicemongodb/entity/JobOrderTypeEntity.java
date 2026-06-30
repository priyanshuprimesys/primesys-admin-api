package com.primesys.adminservicemongodb.entity;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("job_order_types")
public class JobOrderTypeEntity {

    @MongoId
    private ObjectId id;

    @Field("name")
    private String name;

    @Field("module")
    private String module;
}
