package com.primesys.adminserviceserver.service;

import com.primesys.adminservicecommon.dto.division.DivisionListDTO;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.EmailMasterEntity;
import com.primesys.adminservicemongodb.entity.FcmNotificationEntity;
import com.primesys.adminservicemongodb.entity.GpsTrackerReport;
import com.primesys.adminservicemongodb.model.DeviceTestReport;

import java.util.List;
import java.util.Optional;

public interface DivisionLoginService {

    List<DivisionListDTO> getAllDivisionList();

    List<DivisionLoginEntity> getAllDivisionLogins();

    DivisionLoginEntity createDivisionLogin(DivisionLoginEntity userRegistrationDTO);

    DivisionLoginEntity getDivisionDetails(String userName, String password);

    DivisionLoginEntity updateDeviceList(String deviceList, String divisionId);

    List<DivisionLoginEntity> getTrackUserLogins();

    List<DivisionLoginEntity> getDivParents(String divisionId);

    DivisionLoginEntity updateDivisionLogin(DivisionLoginEntity divisionLogin);

    List<FcmNotificationEntity> getFcmNotification(String divisionId);

    Optional<DivisionLoginEntity> addFcmToken(String divisionId, String token, String updatedBy);

    boolean removeFcmToken(String divisionId, String token);

    List<DivisionLoginEntity> getAdminLogins();

    Optional<DivisionLoginEntity> patchModulesList(String divisionId, List<String> modulesList, String modifiedBy);

    Optional<GpsTrackerReport> saveInspectionReport(GpsTrackerReport report);

    Optional<List<GpsTrackerReport>> getAllInspectionReport();

    Optional<List<GpsTrackerReport>> getDivisionInspectionReport(String divisionId);

    Optional<GpsTrackerReport> addDeviceToReport(String reportId, List<DeviceTestReport> devices);

    List<EmailMasterEntity> getReportEmailMaster();

    String getDivisionFromId(String divisionId);
}
