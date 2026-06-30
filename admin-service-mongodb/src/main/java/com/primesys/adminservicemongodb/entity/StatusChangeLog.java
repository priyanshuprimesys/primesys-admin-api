package com.primesys.adminservicemongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusChangeLog implements Serializable {
    private String status;
    private String changedBy;
    private Long changedAt;

}
