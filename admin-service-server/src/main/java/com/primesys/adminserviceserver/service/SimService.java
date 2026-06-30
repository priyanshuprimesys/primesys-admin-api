package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.SimEntity;
import com.primesys.adminserviceserver.response.SimUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SimService {

    /**
     * Parse an uploaded CSV or Excel file and persist its rows as SIM records. Columns are matched by header name
     * (case/format-insensitive), so the Jio layout (ICCID / IMSI / MSISDN) and the Airtel layout (SIM_NO / SIM_IMSI /
     * MOBILE_NUMBER …) both map onto the same {@code SimEntity}.
     *
     * @param file
     *            uploaded .csv / .xlsx / .xls file
     * @param provider
     *            "JIO" or "AIRTEL" — stored on every row
     * @param createdBy
     *            who triggered the upload
     */
    SimUploadResult importSimFile(MultipartFile file, String provider, String createdBy) throws IOException;

    /**
     * Fetch SIM records. When {@code simProvider} is blank, every record is returned; otherwise only rows whose
     * {@code sim_provider} matches (case-insensitive), e.g. "JIO" or "AIRTEL".
     */
    List<SimEntity> getSimRecords(String simProvider);
}
