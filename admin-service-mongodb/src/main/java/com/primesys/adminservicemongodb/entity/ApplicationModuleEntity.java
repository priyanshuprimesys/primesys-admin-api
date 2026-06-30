package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("application_modules")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationModuleEntity {

    @MongoId
    private ObjectId id;

    @Field("name")
    private String name;

    @Field("alias")
    private String alias;

    @Field("description")
    private String description;

    // @Field("")
}
