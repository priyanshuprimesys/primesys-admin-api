package com.primesys.adminservicemongodb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
public class Command {

    @Field("command")
    private String command;

    @Field("timestamp")
    private Long timestamp;

    @Field("delivered_msg")
    private String deliveredMsg;

    @Field("login_name")
    private String loginName;

    @Field("device_response")
    private String deviceResponse;

    @Field("device_response_time")
    private Long deviceResponseTime;
}
