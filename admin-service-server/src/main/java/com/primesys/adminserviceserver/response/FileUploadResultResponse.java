package com.primesys.adminserviceserver.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileUploadResultResponse {
    int validRecords;
    int invalidRecords;
    String errorDescription;
    Boolean dryRun;
    List<DeviceUploadResult> devices;

    // aggregate totals across all devices
    int totalTripsCreated;
    int totalTripsSkipped;
    int totalTripsDuplicate;

    // human-readable one-liner
    String summary;
}
