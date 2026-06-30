package com.primesys.adminserviceserver.request;

import lombok.Data;

import java.util.List;

@Data
public class BulkStatusRequest {
    private List<String> ids;
    private String status;
    private String updatedBy;
}
