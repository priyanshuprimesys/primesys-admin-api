package com.primesys.adminservicemongodb.entity;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "sim_master")
public class SimEntity {

    @MongoId
    ObjectId id;

    /** Jio header: ICCID | Airtel header: SIM_NO */
    @Field("sim_no")
    private String simNo;

    /** Jio header: IMSI | Airtel header: SIM_IMSI */
    @Field("sim_imsi")
    private String simImsi;

    /** Jio header: MSISDN | Airtel header: MOBILE_NUMBER */
    @Field("mobile_number")
    private String mobileNumber;

    @Field("imei")
    private String imei;

    @Field("basket_name")
    private String basketName;

    @Field("sim_status")
    private String simStatus;

    @Field("plan_name")
    private String planName;

    @Field("activation_date")
    private String activationDate;

    @Field("onboarding_date")
    private String onboardingDate;

    @Field("apn1")
    private String apn1;

    /** "JIO" or "AIRTEL" — supplied at upload time, not read from the sheet. */
    @Field("sim_provider")
    private String simProvider;

    @Field("ref_file_name")
    private String refFileName;

    @Field("created_at")
    private long createdAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_at")
    private long updatedAt;

    @Field("updated_by")
    private String updatedBy;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
