package com.primesys.adminserviceserver.modules.report_modules.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportModuleResponseDTO {
    private String id;
    String parentId;
    private String moduleName;
    String description;
    private String displayName;
    private String customDisplayName;
    private Integer customTypeId;
    private Integer displayOrder;
    Integer typeId;
    Boolean active;
    List<ReportModuleResponseDTO> subModules;
}
