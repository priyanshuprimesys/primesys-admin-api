package com.primesys.adminservicemongodb.enums;

public enum NotificationJobStatus {
    PENDING, // waiting for scheduledAt to arrive — cron will pick this up
    RUNNING, // cron locked it and is creating delivery records right now
    COMPLETED, // all inbox records created successfully
    FAILED // job threw an exception; see error_message field
}
