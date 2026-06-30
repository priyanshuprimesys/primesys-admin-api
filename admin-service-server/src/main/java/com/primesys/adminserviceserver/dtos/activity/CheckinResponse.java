package com.primesys.adminserviceserver.dtos.activity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckinResponse {
    private String sessionId;
    private Long checkedInAt;
}
