package com.primesys.adminservicecommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriorityStatusCountDTO {
    private String priority;
    private String status;
    private long count;
}
