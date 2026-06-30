package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.primesys.adminservicemongodb.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data

@Document("division_login_transaction")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DivisionLoginTransactionEntity implements UserDetails {
    @MongoId
    ObjectId id;
    @JsonProperty("parent_id")
    @Field("parent_id")
    Integer parentId;
    @JsonProperty("master_id")
    @Field("master_id")
    ObjectId masterId;
    @JsonProperty("user_login_id")
    @Field("user_login_id")
    Integer userLoginId;

    @JsonProperty("school_id")
    @Field("school_id")
    Integer schoolId;
    @JsonProperty("user_name")
    @Field("user_name")
    String userName;

    @JsonProperty("name")
    @Field("name")
    String name;
    @JsonProperty("password")
    @Field("password")
    String password;

    @JsonProperty("mobile_no")
    @Field("mobile_no")
    String mobileNo;

    @Field("role_id")
    @JsonProperty("role_id")
    Integer roleId;

    @Field("dept_id")
    @JsonProperty("dept_id")
    Integer deptId;

    @Field("county_code")
    @JsonProperty("county_code")
    String countyCode;

    @Field("is_railway_user")
    @JsonProperty("is_railway_user")
    Boolean isRailwayUser;

    @Field("path")
    @JsonProperty("path")
    private String path;

    @Field("device_list")
    @JsonProperty("device_list")
    private String deviceList;

    @Field("county")
    @JsonProperty("county")
    private String countryCode;

    @JsonProperty("report_email_id")
    @Field("report_email_id")
    private String reportEmailId;

    @JsonProperty("report_email_password")
    @Field("report_email_password")
    private String reportEmailIdPassword;

    @JsonProperty("po_no")
    @Field("po_no")
    private String poNo;

    @JsonProperty("po_end_date")
    @Field("po_end_date")
    private String poEndDate;
    @JsonProperty("track_division_id")
    @Field("track_division_id")
    private String trackDivisionId;

    // @Version
    // @JsonProperty("version")
    // private Long version;
    @CreatedDate
    @JsonProperty("created_at")
    @Field("created_at")
    private Long createdAt;
    @LastModifiedDate
    @Field("last_modified")
    @JsonProperty("last_modified")
    private Long lastModified;

    @Field("last_modified_by")
    @JsonProperty("last_modified_by")
    private String lastModifiedBy;
    Role role;
    @Field("report_email_sent")
    @JsonProperty("report_email_sent")
    private Boolean reportEmailSent;
    @Field("email_login_password")
    @JsonProperty("email_login_password")
    private String emailLoginPassword;
    @Field("report_enable")
    private Boolean reportEnable;
    @Field("active_status")
    @JsonProperty("active_status")
    private Boolean activeStatus;
    @Field("whatsapp_group_name")
    @JsonProperty("whatsapp_group_name")
    private String whatsappGroupName;

    @Field("short_name")
    @JsonProperty("short_name")
    private String shortName;

    @Field("modules_list")
    @JsonProperty("modules_list")
    private List<String> modulesList;

    @Field("fcm_token_list")
    @JsonProperty("fcm_token_list")
    private Set<String> fcmTokenList;

    @Field("fcm_updated_at")
    @JsonProperty("fcm_updated_at")
    private long fcmUpdatedAt;

    @Field("transaction_at")
    @JsonProperty("transaction_at")
    private Long transactionAt;

    @Field("ip_address")
    @JsonProperty("ip_address")
    private String ipAddress;

    @Field("user_agent")
    @JsonProperty("user_agent")
    private String userAgent;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }
}
