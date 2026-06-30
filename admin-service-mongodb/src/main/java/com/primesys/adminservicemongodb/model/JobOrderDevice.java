package com.primesys.adminservicemongodb.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class JobOrderDevice {

    @Field("track_division_id")
    private String trackDivisionId;

    @Field("device_imei")
    private Long deviceImei;

    @Field("remark")
    private String remark;

}
