package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.model.DevicePayment;
import com.primesys.adminservicemongodb.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.List;

@Document("module_master")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleMasterEntity {

    @MongoId
    private ObjectId id;
    @Field("module_name")
    String moduleName;
    @Field("display_name")
    String displayName;
    @Field("display_order")
    int displayOrder;
    @Field("type_id")
    private int typeId;
    @Field("sub_modules")
    private List<ModuleMasterEntity> subModules;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
