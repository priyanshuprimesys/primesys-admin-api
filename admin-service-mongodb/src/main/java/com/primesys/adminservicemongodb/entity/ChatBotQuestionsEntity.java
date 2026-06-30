package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.model.Option;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("chat_bot_questions")
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ChatBotQuestionsEntity {
    @MongoId
    ObjectId id;
    @Field("que_id")
    private Integer queId;
    private String question;
    private List<Option> options;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }

}
