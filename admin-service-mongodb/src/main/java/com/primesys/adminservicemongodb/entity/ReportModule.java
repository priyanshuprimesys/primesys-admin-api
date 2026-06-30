package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Document("report_module")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportModule {

    @MongoId
    private ObjectId id;

    @Field("parent_id")
    ObjectId parentId;

    @Field("module_name")
    @Indexed(unique = true)
    private String moduleName;

    @Field("description")
    String description;

    @Field("display_name")
    private String displayName;

    @Field("display_order")
    @Indexed(unique = true)
    private Integer displayOrder;

    @Field("type_id")
    Integer typeId;

    @Field("status")
    Boolean status;

}
