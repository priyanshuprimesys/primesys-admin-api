package com.primesys.adminservicemongodb.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.primesys.adminservicemongodb.enums.LocationSource;
import com.primesys.adminservicemongodb.model.DevicePayment;
import com.primesys.adminservicemongodb.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;
import java.util.List;

@Document("devices")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceEntity {

    @MongoId
    ObjectId id;
    @Field("device_imei")
    Long deviceImei;
    @Field("report_time_margin")
    int reportTimeMargin;
    @Field("report_dist_margin")
    int reportDistMargin;

    @Field("on_track_margin")
    int onTrackMargin;
    @Field("shift_type")
    int shiftType;
    @Field("parent_id")
    Long parentId;
    @Field("division_id")
    String divisionId;
    @Field("student_id")
    String studentId;
    @Field("device_name")
    String deviceName;
    @Deprecated
    @Field("last_name")
    String lastName;
    @Field("device_sim_no")
    String deviceSimNo;
    @Field("device_sim_imei_no")
    String deviceSimImeiNo;
    @Field("device_sim_imsi_no")
    String deviceSimImsiNo;
    @Field("device_no")
    Integer deviceNo;
    @Field("sos_numbers")
    List<String> sosNumbers;
    // @Field("path")
    // String path;
    @Field("show_google_address")
    Boolean showGoogleAddress;
    @Field("report_as_independent_rdps")
    Boolean reportAsIndependentRdps;
    @Field("version")
    Integer version;
    @Field("device_payment")
    DevicePayment devicePayment;
    Location location;
    @CreatedDate
    @Field("created_at")
    private Date createdAt;
    @LastModifiedDate
    @Field("last_modified")
    private Date lastModified;
    @Field("device_type_id")
    private Integer deviceTypeId;
    @Field("device_user_type")
    private String deviceUserType;
    @Field("report_enable")
    private Boolean reportEnable;
    @Field("track_pids")
    private List trackPids;
    @Field("pid")
    String pid;
    @Field("pid_update")
    Long pidUpdate;
    @Field("connected")
    Boolean connected;
    @Field("connected_at")
    Long connectedAt; // epoch seconds, optional
    @Field("disconnected_at")
    Long disconnectedAt; // epoch seconds, optional
    @Field("device_version")
    String deviceVersion;
    @Field("trip_wise_report")
    private Boolean tripWiseReport;
    @Field("sim_service_provider")
    private String simServiceProvider;

    // Always store the SIM service provider in lowercase, regardless of write path.
    public void setSimServiceProvider(String simServiceProvider) {
        this.simServiceProvider = simServiceProvider == null ? null : simServiceProvider.toLowerCase();
    }

    // Always expose the SIM service provider in lowercase, even for legacy data stored mixed-case.
    public String getSimServiceProvider() {
        return simServiceProvider == null ? null : simServiceProvider.toLowerCase();
    }

    @Field("activation_date")
    private Long activationDate;
    @Field("updated_by")
    private String updatedBy;
    @Field("updated_at")
    private long updatedAt;
    @Field("active_status")
    @JsonProperty("active_status")
    private Boolean activeStatus;

    @Field("location_source")
    private LocationSource locationSource;

    public String getId() {
        if (id != null)
            return id.toString();
        else
            return null;
    }

    /** Max staleness allowed before a "connected" flag is no longer trusted (node-crash fallback). */
    private static final long CONNECTED_STALE_FALLBACK_SECS = 600;

    /**
     * Socket-accurate connectivity. The Erlang server mirrors live syn-registry state into `connected`; pidUpdate
     * freshness is a fallback so a node hard-crash that leaves a stale connected=true still expires.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isConnected() {
        if (!Boolean.TRUE.equals(connected)) {
            return false; // explicit disconnect → instant, trusted
        }
        if (pidUpdate == null) {
            return false;
        }
        long nowSecs = System.currentTimeMillis() / 1000;
        return (nowSecs - pidUpdate) < CONNECTED_STALE_FALLBACK_SECS;
    }

    /**
     * Single source of truth for the display device name: the device's name concatenated with its device number using a
     * "-" separator (e.g. "Rupesh-1"). Use this wherever a device name needs to be shown so the format stays consistent
     * across reports, beats and commands.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String buildDeviceName() {
        return com.primesys.adminservicemongodb.util.DeviceNameUtil.format(deviceName, deviceNo);
    }
}
