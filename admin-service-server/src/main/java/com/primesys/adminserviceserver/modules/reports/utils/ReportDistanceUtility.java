package com.primesys.adminserviceserver.modules.reports.utils;

import com.primesys.adminservicemongodb.model.DeviceHistory;

import java.util.Map;

public final class ReportDistanceUtility {
    public static boolean distanceDiffLessThan50M(DeviceHistory h) {
        double diff = extractDistanceDiff(h.getNearestRdps());
        return diff < 50;
    }

    public static boolean distanceDiffMore5OM(DeviceHistory h) {
        double diff = extractDistanceDiff(h.getNearestRdps());
        return diff > 50;
    }

    public static boolean distanceDiffMore10OM(DeviceHistory h) {
        double diff = extractDistanceDiff(h.getNearestRdps());
        return diff > 100;
    }

    public static boolean distanceDiffLess100M(DeviceHistory h) {
        double diff = extractDistanceDiff(h.getNearestRdps());
        return diff < 100;
    }

    public static double kmDistValue(Object rdps) {
        if (!(rdps instanceof Map<?, ?> map))
            return 0;

        double km = map.get("kilometer") instanceof Number n ? n.doubleValue() : 0;

        double dist = map.get("distance") instanceof Number n ? n.doubleValue() : 0;

        return km + dist / 1000.0;
    }

    private static double extractDistanceDiff(Object rdpsObj) {

        if (!(rdpsObj instanceof Map<?, ?> map))
            return Double.MAX_VALUE;

        Object d = map.get("distance_diff");

        if (d instanceof Number)
            return ((Number) d).doubleValue();

        if (d instanceof String) {
            try {
                return Double.parseDouble((String) d);
            } catch (Exception ignored) {
            }
        }

        return Double.MAX_VALUE;
    }
}
