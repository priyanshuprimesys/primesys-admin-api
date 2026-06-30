package com.primesys.adminserviceserver.request;

import lombok.Data;

@Data
public class ResolveTicketRequest {
    /** FIXED | WONT_FIX | DUPLICATE | INVALID | CANNOT_REPRODUCE */
    private String resolution;
    private String resolvedBy;
    private String note;
}
