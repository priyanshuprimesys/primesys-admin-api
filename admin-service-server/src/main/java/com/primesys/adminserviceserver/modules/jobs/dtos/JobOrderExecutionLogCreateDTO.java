package com.primesys.adminserviceserver.modules.jobs.dtos;

import com.primesys.adminservicemongodb.enums.JobOrderExecutionStatus;
import com.primesys.adminservicemongodb.model.JobOrderDevice;
import lombok.*;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Setter
@Getter
public class JobOrderExecutionLogCreateDTO {
    private ObjectId jobOrderId;
    private Instant jobStartAt;
    private ObjectId jobOrderTypeId;
    private Instant jobEndAt;
    private List<JobOrderDevice> failedDevices;
    private String remark;
    private String trackDivisionId;
    private String status;
    private String createdBy;
}
