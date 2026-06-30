package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("division_meta_data")
public class DivisionMetaDataEntity {
    @MongoId
    ObjectId id;

    @Field("divisions")
    private Integer totalDivision;

    @Field("active_divisions")
    private Integer totalActiveDivision;

    @Field("inactive_divisions")
    private Integer totalInactiveDivision;

    @Field("devices")
    private Integer totalDevices;

    @Field("active_devices")
    private Integer activeDevices;

    @Field("inactive_devices")
    private Integer inactiveDevices;

    @Field("railway_users")
    private Integer railwayUsers;

    @Field("non_railway_users")
    private Integer nonRailwayUsers;

}
