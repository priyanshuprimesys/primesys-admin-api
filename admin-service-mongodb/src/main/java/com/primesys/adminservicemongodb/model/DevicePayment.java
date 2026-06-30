package com.primesys.adminservicemongodb.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("devices_payment_transaction")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DevicePayment {

    @JsonProperty("payment_plan_id")
    @Field("payment_plan_id")
    private int paymentPlanId;
    @JsonProperty("device_imei")
    @Field("device_imei")
    private Long DeviceImei;
    @JsonProperty("payment_renew_date")
    @Field("payment_renew_date")
    private long paymentRenewDate;
    @JsonProperty("expiry_date")
    @Field("expiry_date")
    private long expiryDate;
    @JsonProperty("updated_by")
    @Field("updated_by")
    private String updatedBy;

}