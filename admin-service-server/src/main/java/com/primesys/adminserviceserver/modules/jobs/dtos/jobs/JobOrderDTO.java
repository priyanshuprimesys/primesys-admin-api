package com.primesys.adminserviceserver.modules.jobs.dtos.jobs;

import com.primesys.adminservicemongodb.enums.JobOrderStatus;
import lombok.*;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class JobOrderDTO {
    ObjectId id;
    private String jobName;
    private JobOrderStatus status;
    private List<String> trackDivisionIds;
    private String typeId;
    private Instant startAt;
    private Instant endAt;
    private Instant startFrom;
    private Instant holdUpto;
    private String createdBy;
}
