package com.primesys.adminserviceserver.request;

import lombok.Data;

@Data
public class LinkTicketRequest {
    private String linkedTicketId;
    /** BLOCKS | RELATES_TO | DUPLICATES */
    private String linkType;
    private String linkedBy;
}
