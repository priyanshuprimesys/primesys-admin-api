package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.model.DeviceTestReport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Document(collection = "gps_tracker_reports")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GpsTrackerReport {
    @MongoId
    ObjectId id;
    private String divisionId; // acts as the main _id

    private String divisionName;
    private Long reportDate;
    private List<DeviceTestReport> devices; // nested list of device reports

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
