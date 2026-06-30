package com.primesys.adminserviceserver.dtos.location;

import com.primesys.adminservicemongodb.entity.DeviceLocation;

import java.util.List;

public record CreateDeletedLocationDTO(DeviceLocation deviceLocation, String deletedBy) {
}
