package com.primesys.adminservicemongodb.enums;

public enum AppNotificationStatus {
    DRAFT, // created but not sent yet
    ACTIVE, // sent and visible to users
    EXPIRED // past expiry date, no longer shown
}
