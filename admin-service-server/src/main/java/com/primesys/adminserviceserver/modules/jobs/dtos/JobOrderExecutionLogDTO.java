package com.primesys.adminserviceserver.modules.jobs.dtos;

import com.primesys.adminservicemongodb.enums.JobOrderExecutionStatus;
import com.primesys.adminservicemongodb.model.JobOrderDevice;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
@Getter
public class JobOrderExecutionLogDTO {
    private ObjectId id;
    private ObjectId jobOrderId;
    private Instant jobStartAt;
    private ObjectId jobOrderTypeId;
    private Instant jobEndAt;
    private List<JobOrderDevice> failedDevices;
    private String remark;
    private String trackDivisionId;
    private JobOrderExecutionStatus status;
    private String createdBy;
    private String updatedBy;
}
