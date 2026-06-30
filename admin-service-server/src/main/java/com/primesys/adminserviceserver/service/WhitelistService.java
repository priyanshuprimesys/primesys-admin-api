package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.PendingWhitelistEntity;
import com.primesys.adminserviceserver.request.WhitelistRequest;

import java.util.List;

public interface WhitelistService {

    /** Returns the whitelist entries, newest first. Optional status filter. */
    List<PendingWhitelistEntity> getWhitelist(String status);

    /**
     * Creates pending whitelist (FN / SOS) entries for a single device, building the provider-specific command string.
     * Any existing PENDING entry for the same device and command type is replaced so only the latest pending whitelist
     * remains.
     */
    List<PendingWhitelistEntity> createWhitelist(WhitelistRequest request);

    /**
     * Updates the status of a single whitelist entry, keyed by its own id (one FN or one SOS row).
     *
     * @param id
     *            the entry's MongoId
     * @param status
     *            target status (PENDING / COMPLETED, case-insensitive)
     * @param updatedBy
     *            who performed the change (optional, stored for audit)
     */
    PendingWhitelistEntity updateStatus(String id, String status, String updatedBy);

    /**
     * Updates the status of every whitelist entry for a device (both FN and SOS) in one call, keyed by device imei.
     * Returns the updated entries.
     *
     * @param deviceImei
     *            the device imei
     * @param status
     *            target status (PENDING / COMPLETED, case-insensitive)
     * @param updatedBy
     *            who performed the change (optional, stored for audit)
     */
    List<PendingWhitelistEntity> updateStatusByDeviceImei(Long deviceImei, String status, String updatedBy);
}
