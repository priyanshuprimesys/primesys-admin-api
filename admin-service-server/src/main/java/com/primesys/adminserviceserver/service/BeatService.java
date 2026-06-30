package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.model.BeatGroupByFileDTO;
import com.primesys.adminservicecommon.dto.DeviceBeatDto;
import com.primesys.adminservicemongodb.entity.BeatEntity;
import com.primesys.adminserviceserver.request.DeviceBeatRequest;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;

import java.util.List;
import java.util.Optional;

public interface BeatService {
    List<BeatEntity> createBeat(BeatEntity beatEntity, boolean isMultiple);

    List<DeviceBeatDto> getDeviceBeat(Long deviceImei);

    Optional<FileUploadResultResponse> createBeatDeviceNo(DeviceBeatRequest beatReq, List<List<String>> excelData,
            boolean isMultiple, String fileName, boolean dryRun);

    Optional<FileUploadResultResponse> createBeatManual(DeviceBeatRequest beatReq, boolean dryRun);

    List<DeviceBeatDto> getDeviceTypeBeat(String divisionId, Integer deviceType);

    Optional<BeatEntity> updateBeat(DeviceBeatRequest beat);

    Optional<BeatEntity> deleteBeat(String beatId, String updatedBy);

    List<BeatEntity> approveMultipleBeats(String beatId, String updatedBy);

    List<BeatGroupByFileDTO> getUnapprovedGroupedByRefFile();

    Optional<FileUploadResultResponse> createBeatHourly(DeviceBeatRequest beat, List<List<String>> excelData, boolean b,
            String s);

    Optional<Integer> deleteBeataApprovalFile(String refFileName, String updatedBy);
}
