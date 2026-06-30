package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Document("report_module_master")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportModuleMaster {

    @MongoId
    ObjectId id;

    @Field("module_id")
    private String moduleId;

    @Field("sub_module_id")
    private String subModuleId;

    @Field("description")
    private String description;

    @Field("status")
    private Boolean status;
}
