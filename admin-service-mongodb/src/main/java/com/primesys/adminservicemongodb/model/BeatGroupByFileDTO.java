package com.primesys.adminservicemongodb.model;

import com.primesys.adminservicemongodb.entity.BeatEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BeatGroupByFileDTO {
    private String refFileName;
    private String division_id;
    private String division_name;
    private List<DeviceGroupDTO> devices;
    private Long createdAt;
    private Boolean activeStatus;
    // constructor, getters, setters
}
