package com.primesys.adminservicemongodb.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserReportModuleModel {
    @Field("module_id")
    private String moduleId;
    @Field("module_parent_id")
    private String moduleParentId;
    @Field("module_name")
    String moduleName;
    @Field("display_name")
    String displayName;
    @Field("custom_display_name")
    private String customDisplayName;
    @Field("active")
    private Boolean active;
    @Field("display_order")
    private Integer displayOrder;
    @Field("type_id")
    private Integer typeId;
    // @Field("is_trip_wise")
    // private Boolean isTripWise;
    @Field("sub_modules")
    List<UserReportModuleModel> subModules;
}
