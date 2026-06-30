package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferLog {

    @Field("from_assignee")
    private String from; // old assignee userId

    @Field("from_name")
    private String fromName; // old assignee display name

    @Field("to_assignee")
    private String to; // new assignee userId

    @Field("to_name")
    private String toName; // new assignee display name

    @Field("transferred_by")
    private String transferredBy; // userId who initiated the transfer

    @Field("transferred_by_name")
    private String transferredByName;

    @Field("reason")
    private String reason;

    @Field("transferred_at")
    private Long transferredAt; // epoch ms
}
