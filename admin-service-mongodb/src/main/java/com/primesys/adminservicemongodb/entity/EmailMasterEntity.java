package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("email_master")
public class EmailMasterEntity {
    @MongoId
    ObjectId id;
    private String email;
    private String password;
    private boolean active_status;
    private String login_password;
    private String mobile_no;
    private List<String> divisions;
    private int divisionsCount;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
