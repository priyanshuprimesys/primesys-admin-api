package com.primesys.adminserviceserver.request;

import lombok.Data;

@Data
public class CloseTicketRequest {
    private String closedBy;
    private String note;
}
