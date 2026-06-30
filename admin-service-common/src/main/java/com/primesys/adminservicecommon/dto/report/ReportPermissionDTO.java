package com.primesys.adminservicecommon.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReportPermissionDTO {
    String id;
    String userName;
    String name;
    Integer deptId;
    String trackDivisionId;
    List<String> moduleList;
}
