package com.primesys.adminservicemongodb.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class Commands {

    @Field("latestFN_set")
    private Command latestFnSet;

    @Field("latestFN")
    private Command latestFn;

    @Field("latestSOS_set")
    private Command latestSosSet;

    @Field("latestSOS")
    private Command latestSos;

    @Field("latestHBT_set")
    private Command latestHbtSet;

    @Field("latestHBT")
    private Command latestHbt;

    @Field("latestTIMER_set")
    private Command latestTimerSet;

    @Field("latestTIMER")
    private Command latestTimer;

    @Field("latestPERIOD_set")
    private Command latestPeriodSet;

    @Field("latestPERIOD")
    private Command latestPeriod;

    @Field("latestSTATUS")
    private Command latestStatus;
    @Field("latestPARAM")
    private Command latestParam;
}
