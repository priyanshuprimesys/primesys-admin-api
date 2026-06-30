package com.primesys.adminserviceserver.modules.reports.dtos;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TripStatusReportSummaryCreateDTO {
    private String name;
    private String path;
    private String deviceOff;
    private String tripCompleted;
    private String tripNotCompleted;
    private String offTrack;
    private String overSpeed;
    private String delayedStart;
    private String trackDivisionId;
    private long reportOfTheDay;
    private int deviceTypeId;
    private int shiftType;
}
