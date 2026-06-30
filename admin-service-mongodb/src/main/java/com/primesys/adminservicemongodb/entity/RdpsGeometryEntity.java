package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.primesys.adminservicemongodb.model.GeoLocation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document("rdps_geometry")
public class RdpsGeometryEntity {
    @MongoId
    ObjectId id;
    @JsonProperty("geo_location")
    @Field("geo_location")
    GeoLocation geoLocation;
    @Field("kilometer")
    Long kilometer;
    @Field("distance")
    Long distance;
    @JsonInclude(JsonInclude.Include.ALWAYS) // Always include this field
    @JsonProperty("feature_code")
    @Field("feature_code")
    Integer featureCode;
    @Field("latitude")
    Double latitude;
    @Field("longitude")
    Double longitude;
    @JsonProperty("division_id")
    @Field("division_id")
    String divisionId;
    @JsonProperty("active_status")
    @Field("active_status")
    Boolean activeStatus;
    @JsonProperty("approved_status")

    @Field("approved_status")
    Boolean approvedStatus;
    @JsonInclude(JsonInclude.Include.ALWAYS) // Always include this field
    @JsonProperty("feature_image")
    @Field("feature_image")
    String featureImage;
    Long version;
    @JsonProperty("created_at")

    @Field("created_at")
    Date createdAt;
    @JsonProperty("last_modified")

    @Field("last_modified")
    Date updatedAt;
    @JsonProperty("updated_by")

    @Field("updated_by")
    String updatedBy;
    @JsonInclude(JsonInclude.Include.ALWAYS) // Always include this field
    @JsonProperty("feature_detail")
    @Field("feature_detail")
    String featureDetail;
    @JsonInclude(JsonInclude.Include.ALWAYS) // Always include this field
    @JsonProperty("section")
    @Field("section")
    String section;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
