package com.primesys.adminserviceserver.utility;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateTimeUtility {

    private final static ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    public static String InstantToString(Instant instant) {
        return instant.atZone(zoneId).toString();
    }

    public static Instant StringToInstant(String time) {
        return LocalDateTime.parse(time).atZone(zoneId).toInstant();
    }

    public static Long toMidNightEpoch() {
        Instant instant = Instant.now();
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        ZonedDateTime midNight = zonedDateTime.truncatedTo(ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0)
                .withNano(0);
        return midNight.toInstant().getEpochSecond();
    }

    public static Long dateToMidNightEpoch(Long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        ZonedDateTime midNight = zonedDateTime.truncatedTo(ChronoUnit.DAYS).withHour(0).withMinute(0).withSecond(0)
                .withNano(0);
        return midNight.toInstant().getEpochSecond();
    }

    public static Long currentEpoch() {
        Instant instant = Instant.now();
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        Long epochSecond = zonedDateTime.toEpochSecond();
        return epochSecond;
    }

    public static String epochToDateAndTime(long epoch) {
        Instant instant = Instant.ofEpochSecond(epoch);
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public static String epochToDate(long epoch) {
        Instant instant = Instant.ofEpochSecond(epoch);
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public static String epochToHourMinute(Long epoch) {

        Instant instant = Instant.ofEpochSecond(epoch);
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);

        return zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

}
