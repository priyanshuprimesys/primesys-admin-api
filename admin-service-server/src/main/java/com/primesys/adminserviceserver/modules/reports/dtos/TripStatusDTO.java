package com.primesys.adminserviceserver.modules.reports.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TripStatusDTO {
    Integer deviceNo;
    Long reportDate;
    String status;
}
