package com.primesys.adminserviceserver.request;

import lombok.Data;

@Data
public class ReopenTicketRequest {
    private String reopenedBy;
    private String note;
}
