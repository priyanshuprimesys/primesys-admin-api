package com.primesys.adminservicemongodb.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class DeviceInfo {

    @Field("device_imei")
    private Long deviceImei;

    @Field("report_time_margin")
    private Integer reportTimeMargin;

    @Field("report_dist_margin")
    private Integer reportDistMargin;

    @Field("on_track_margin")
    private Integer onTrackMargin;

    @Field("device_name")
    private String deviceName;

    @Field("device_sim_no")
    private String deviceSimNo;

    @Field("device_sim_imei_no")
    private String deviceSimImeiNo;

    @Field("device_no")
    private Integer deviceNo;

    @Field("report_enable")
    private Boolean reportEnable;

    @Field("device_version")
    private String deviceVersion;
}
