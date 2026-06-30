package com.primesys.adminserviceserver.request;

import lombok.Data;

@Data
public class ChangeStatusRequest {
    private String status;
    private String updatedBy;
    private String note;
}
