package com.primesys.adminserviceserver.modules.reports.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.model.ReportEmailLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportEmailResponseDTO {
    String divisionId;
    String divisionName;
    String trackDivisionId;
    ReportEmailLog reportEmailLog;
}
