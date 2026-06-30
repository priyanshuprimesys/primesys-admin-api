package com.primesys.adminserviceserver.utility;

import java.util.HashMap;
import java.util.Map;

public class TimeCalculator {

    public static Map<String, Long> getTime(String startTime, String endTime) {
        long startSec = toSeconds(startTime);
        long endSec = toSeconds(endTime);

        long tripTime;
        if (endSec < startSec) {
            tripTime = (86400 - startSec) + endSec;
        } else {
            tripTime = endSec - startSec;
        }

        Map<String, Long> result = new HashMap<>();
        result.put("startTime", startSec);
        result.put("tripTime", tripTime);
        result.put("endTime", endSec);

        return result;
    }

    private static int toSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 3600 + minutes * 60;
    }

    // public static void main(String[] args) {
    // Map<String, Integer> result = getTime("23:00", "01:00");
    // System.out.println(result); // Output: {starttime=82800, triptime=7200, endtime=3600}
    // }
}
