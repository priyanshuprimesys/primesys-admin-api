package com.primesys.adminserviceserver.modules.user.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO {
    private String id;
    private Integer parentId;
    private Integer userLoginId;
    private String userName;
    private String name;
    private String mobileNo;
    private Integer roleId;
    private Integer deptId;
    private String path;
    private String trackDivisionId;
    private Boolean activeStatus;
}
