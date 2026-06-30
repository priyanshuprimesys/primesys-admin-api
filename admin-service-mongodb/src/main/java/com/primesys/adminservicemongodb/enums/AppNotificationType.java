package com.primesys.adminservicemongodb.enums;

public enum AppNotificationType {
    UPDATE, // new app version available on Play Store
    FEATURE, // highlight of a new feature in current version
    GENERAL, // general announcement
    MAINTENANCE // scheduled downtime / maintenance window
}
