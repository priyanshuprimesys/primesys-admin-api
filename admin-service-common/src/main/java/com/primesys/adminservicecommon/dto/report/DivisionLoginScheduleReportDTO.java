package com.primesys.adminservicecommon.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.primesys.adminservicemongodb.model.DeviceImeiNoOnly;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DivisionLoginScheduleReportDTO {
    private String path;
    private String trackDivisionId;
    private List<DeviceImeiNoOnly> deviceNoImeis;
    private String name;
    private int shiftType;
}
