package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.model.Location;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("location_backup")
public class LocationBackupEntity extends Location {
}
