package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("token_admin")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenEntity {
    @MongoId
    ObjectId id;

    String user;
    boolean revoked;
    boolean expired;
    String token;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
