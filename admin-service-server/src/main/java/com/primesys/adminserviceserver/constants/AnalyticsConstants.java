package com.primesys.adminserviceserver.constants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AnalyticsConstants {
    // Fixed order for statuses and priorities (zero-fill uses these)
    public static final List<String> STATUS_ORDER = Arrays.asList("SOFTCLOSE", "CLOSE", "UNDEROBSERVATION",
            "INPROGRESS", "OPEN");

    public static final List<String> PRIORITY_ORDER = Arrays.asList("HIGH", "MEDIUM", "LOW");

    // No defaults for category/tags — they'll be auto-detected if not provided in request
    public static final Map<String, List<String>> DEFAULT_EXTRA_FIELD_KEYS = Map.of("issue_status", STATUS_ORDER,
            "priority", PRIORITY_ORDER);
}
