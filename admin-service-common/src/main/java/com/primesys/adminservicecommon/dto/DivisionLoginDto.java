package com.primesys.adminservicecommon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DivisionLoginDto {
    String userName;
    String mobileNo;
    String emailID;
    Integer roleId;
    String divisionId;
    String socketPort;
    String socketUrl;
    String distUnit;
    String password;
}
