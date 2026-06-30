package com.primesys.adminserviceserver.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManualBeatTrip {
    int tripNo;
    String startKm; // "0.000"
    String endKm; // "10.000"
    String startTime; // "HH:mm"
    String endTime; // "HH:mm"
}
