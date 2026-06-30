package com.primesys.adminservicemongodb.entity;

import com.primesys.adminservicemongodb.model.Commands;
import com.primesys.adminservicemongodb.model.DeviceInfo;
import com.primesys.adminservicemongodb.model.Location;
import com.primesys.adminservicemongodb.model.TripInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "device_info_master")
public class DeviceInfoMaster {
    @Id
    private String id;
    @Field("device_imei")
    private Long deviceImei;
    private Commands commands;
    @Field("device_info")
    private DeviceInfo deviceInfo;
    @Field("trip_info")
    private List<TripInfo> tripInfo;
    private TodayLocationEntity location;

}
