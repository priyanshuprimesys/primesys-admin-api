package com.primesys.adminserviceserver.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferTicketRequest {

    private String id; // MongoDB _id of the ticket

    private String toAssignee; // new assignee userId
    private String toAssigneeName; // new assignee display name

    private String transferredBy; // userId of the person doing the transfer
    private String reason; // optional reason / note for the transfer
}
