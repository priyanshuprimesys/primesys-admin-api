package com.primesys.adminservicecommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagUsageDTO {
    private String tag;
    private long count;
}
