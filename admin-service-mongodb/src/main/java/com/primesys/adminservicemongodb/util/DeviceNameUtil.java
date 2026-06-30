package com.primesys.adminservicemongodb.util;

/**
 * Single source of truth for formatting a device's display name.
 * <p>
 * A device name is always shown as the device's name joined to its device number with a "-" separator (e.g.
 * "Rupesh-1"). Route every place that builds a device display name through {@link #format(String, Object)} so the
 * format stays consistent across reports, beats, commands and imports.
 */
public final class DeviceNameUtil {

    private DeviceNameUtil() {
    }

    /**
     * Builds the display device name as {@code name + "-" + deviceNo}. A null name is treated as empty so the suffix is
     * always applied.
     * <p>
     * Idempotent: any trailing {@code "-" + deviceNo} already present on {@code name} is stripped before the suffix is
     * re-applied, so passing either a bare name ("Rupesh") or an already-formatted one ("Rupesh-1") — or a value that
     * was doubled previously ("Rupesh-1-1") — all yield "Rupesh-1". This is what keeps the name from compounding when
     * the stored device_name and a re-format both add the suffix.
     */
    public static String format(String name, Object deviceNo) {
        String base = (name == null) ? "" : name.trim();
        String suffix = "-" + deviceNo;
        while (base.endsWith(suffix))
            base = base.substring(0, base.length() - suffix.length()).trim();
        return base + "-" + deviceNo;
    }
}
