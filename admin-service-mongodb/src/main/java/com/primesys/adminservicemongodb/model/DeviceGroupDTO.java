package com.primesys.adminservicemongodb.model;

import com.primesys.adminservicemongodb.entity.BeatEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceGroupDTO {
    private String deviceImei;
    private List<BeatEntity> beats;
}