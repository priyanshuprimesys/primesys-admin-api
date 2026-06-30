package com.primesys.adminserviceserver.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request to create device whitelist (FN / SOS) numbers for a single device.
 * <p>
 * The resulting SMS command string is built per sim provider (JIO / AIRTEL), because the on-device command format
 * differs between providers.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhitelistRequest {

    Long deviceImei;
    String deviceName;
    /** JIO or AIRTEL. If null, it is resolved from the device's sim service provider. */
    String simProvider;
    Long parentId;
    String loginName;

    /** Family numbers for the FN command (ordered, name + number). */
    List<Contact> familyNumbers;

    /** SOS numbers for the SOS command (ordered). */
    List<String> sosNumbers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Contact {
        String name;
        String number;
    }
}
