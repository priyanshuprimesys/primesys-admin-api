package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document("otp")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpEntity {
    @MongoId
    ObjectId id;
    @Indexed(unique = true) // Ensure userId is unique
    private String userId; // Change phoneNumber to userId
    private String otp;
    private LocalDateTime expiresAt;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
