package com.primesys.adminserviceserver.modules.reports.dtos.division_report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class DeviceSos {
    @Field("_id")
    String _id;
    @Field("device_imei")
    Long deviceImei;
    @Field("status")
    String status;
    @Field("voltage_level")
    String voltageLevel;
    @Field("gsm_signal_strength")
    String gsmSignalStrength;
    Long timestamp;

}
