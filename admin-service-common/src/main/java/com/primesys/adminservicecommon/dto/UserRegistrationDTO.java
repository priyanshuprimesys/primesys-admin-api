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
public class UserRegistrationDTO {

    private String userName;
    private String name;
    private String password;
    private String mobileNo;
    private boolean isRailwayUser;
    private String role;

}
