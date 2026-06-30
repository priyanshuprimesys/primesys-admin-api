package com.primesys.adminservicecommon.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateReportPermissionDTO {
    String divisionId;
    List<String> modulesList;
    String modifiedBy;
}
