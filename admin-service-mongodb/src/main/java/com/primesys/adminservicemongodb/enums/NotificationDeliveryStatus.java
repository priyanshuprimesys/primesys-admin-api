package com.primesys.adminservicemongodb.enums;

public enum NotificationDeliveryStatus {
    UNREAD, // inbox entry created by the job, user has not opened it yet
    READ // user opened or dismissed the notification in the app
}
