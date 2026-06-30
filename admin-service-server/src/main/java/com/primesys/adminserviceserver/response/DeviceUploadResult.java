package com.primesys.adminserviceserver.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceUploadResult {
    private int deviceNo;
    private long deviceImei;
    private int tripsCreated;
    private int tripsSkipped; // N/A slots
    private int tripsDuplicate; // same (device,tripNo) appeared twice in batch
    private List<Integer> createdTripNos;
}
