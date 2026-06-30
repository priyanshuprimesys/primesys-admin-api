package com.primesys.adminserviceserver.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to change a whitelist entry's status (e.g. PENDING → COMPLETED).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WhitelistStatusUpdateRequest {

    /** Target status — PENDING or COMPLETED (case-insensitive). */
    String status;

    /** Who performed the change (stored for audit). Optional. */
    String updatedBy;
}
