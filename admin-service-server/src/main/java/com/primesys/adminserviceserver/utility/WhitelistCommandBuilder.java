package com.primesys.adminserviceserver.utility;

import com.primesys.adminserviceserver.request.WhitelistRequest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds the on-device SMS whitelist command strings (FN / SOS).
 * <p>
 * The command body differs per sim provider, so all provider-specific formatting lives here behind {@link #buildFn} /
 * {@link #buildSos}. JIO is implemented per the documented device format:
 *
 * <pre>
 *   FN  -&gt; FN,A,&lt;name1&gt;,&lt;number1&gt;,&lt;name2&gt;,&lt;number2&gt;,...
 *   SOS -&gt; SOS,A,&lt;number1&gt;,&lt;number2&gt;,&lt;number3&gt;
 * </pre>
 */
public final class WhitelistCommandBuilder {

    public static final String PROVIDER_JIO = "JIO";
    public static final String PROVIDER_AIRTEL = "AIRTEL";

    public static final String TYPE_FN = "FN";
    public static final String TYPE_SOS = "SOS";

    private static final String ADD_ACTION = "A";

    private WhitelistCommandBuilder() {
    }

    /** Builds the FN (family numbers) command, or null when there are no numbers. */
    public static String buildFn(String provider, List<WhitelistRequest.Contact> contacts) {
        List<WhitelistRequest.Contact> unique = dedupeContacts(contacts);
        if (unique.isEmpty()) {
            return null;
        }
        // Currently JIO and AIRTEL share the documented FN format. Adjust the
        // AIRTEL branch here (switch on normalizeProvider(provider)) once the
        // exact Airtel format is confirmed.
        StringBuilder sb = new StringBuilder(TYPE_FN).append(',').append(ADD_ACTION);
        for (WhitelistRequest.Contact c : unique) {
            sb.append(',').append(safe(c.getName())).append(',').append(safe(c.getNumber()));
        }
        return sb.toString();
    }

    /** Builds the SOS command, or null when there are no numbers. */
    public static String buildSos(String provider, List<String> numbers) {
        List<String> unique = dedupeNumbers(numbers);
        if (unique.isEmpty()) {
            return null;
        }
        // Currently JIO and AIRTEL share the documented SOS format. Adjust the
        // AIRTEL branch here (switch on normalizeProvider(provider)) once the
        // exact Airtel format is confirmed.
        StringBuilder sb = new StringBuilder(TYPE_SOS).append(',').append(ADD_ACTION);
        for (String n : unique) {
            sb.append(',').append(n);
        }
        return sb.toString();
    }

    public static String normalizeProvider(String provider) {
        if (provider == null) {
            return PROVIDER_JIO;
        }
        String p = provider.trim().toUpperCase();
        return p.isEmpty() ? PROVIDER_JIO : p;
    }

    /** Removes duplicate numbers (keeps first occurrence) and blanks. */
    private static List<String> dedupeNumbers(List<String> numbers) {
        List<String> out = new ArrayList<>();
        if (numbers == null) {
            return out;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String n : numbers) {
            if (n == null) {
                continue;
            }
            String t = n.trim();
            if (!t.isEmpty() && seen.add(t)) {
                out.add(t);
            }
        }
        return out;
    }

    /** Removes contacts with duplicate numbers (keeps first occurrence) and blanks. */
    private static List<WhitelistRequest.Contact> dedupeContacts(List<WhitelistRequest.Contact> contacts) {
        List<WhitelistRequest.Contact> out = new ArrayList<>();
        if (contacts == null) {
            return out;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (WhitelistRequest.Contact c : contacts) {
            if (c == null || c.getNumber() == null) {
                continue;
            }
            String t = c.getNumber().trim();
            if (!t.isEmpty() && seen.add(t)) {
                out.add(c);
            }
        }
        return out;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
