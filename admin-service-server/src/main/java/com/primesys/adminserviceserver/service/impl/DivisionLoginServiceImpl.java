package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicecommon.dto.DeviceDto;
import com.primesys.adminservicecommon.dto.division.DivisionListDTO;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.*;
import com.primesys.adminservicemongodb.model.DeviceTestReport;
import com.primesys.adminservicemongodb.repository.*;
import com.primesys.adminserviceserver.config.PasswordGenerator;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.ResourceNotFoundException;
import com.primesys.adminserviceserver.mapper.DivisionMapper;
import com.primesys.adminserviceserver.service.DeviceService;
import com.primesys.adminserviceserver.service.DivisionLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DivisionLoginServiceImpl implements DivisionLoginService {

    private final DivisionLoginRepository divisionLoginRepository;
    private final FcmNotificationRepository fcmNotificationRepository;
    private final GpsTrackerReportRepository gpsTrackerReportRepository;

    private final DivisionLoginTransactionRepository divisionLoginTransactionRepository;
    private final EmailMasterRepository emailMasterRepository;
    private final MongoTemplate mongoTemplate;
    @Autowired
    @Lazy
    private DeviceService deviceService;

    public List<DivisionListDTO> getAllDivisionList() {
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository
                .findAll(Sort.by(Sort.Direction.DESC, "id"));

        if (divisionLoginEntities.isEmpty()) {
            throw new ResourceNotFoundException("No division found");
        }

        List<DivisionListDTO> divisionListDTOS = divisionLoginEntities.stream()
                .map(divisionLoginEntity -> DivisionMapper.toDivisionListDTO(divisionLoginEntity)).toList();
        return divisionListDTOS;
    }

    public List<DivisionLoginEntity> getAllDivisionLogins() {
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository
                .findAll(Sort.by(Sort.Direction.DESC, "id"));
        if (divisionLoginEntities.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        }
        return divisionLoginEntities;
        // return divisionLoginEntities.stream()
        // .map(device -> UserRegistrationDTO.builder().mobileNo(device.getMobileNo()).name(device.getName())
        // .userName(device.getUsername()).password(device.getPassword())
        // .isRailwayUser(device.getIsRailwayUser()).role(device.getRoleName()).build())
        // .collect(Collectors.toList());
    }

    public DivisionLoginEntity createDivisionLogin(DivisionLoginEntity userRegistrationDTO) {

        Optional<DivisionLoginEntity> existingDivision = divisionLoginRepository
                .findByUserName(userRegistrationDTO.getUsername());

        if (existingDivision.isPresent()) {
            throw new ResourceNotFoundException(ErrorCode.DIVISION_DUPLICATE_FAILED.toString());
        }

        String randomPassword = PasswordGenerator.generateRandomPassword();
        userRegistrationDTO.setPassword(randomPassword);
        userRegistrationDTO.setActiveStatus(true);
        userRegistrationDTO.setDeviceList(normalizeDeviceList(userRegistrationDTO.getDeviceList()));

        if (userRegistrationDTO.getModulesList() == null || userRegistrationDTO.getModulesList().isEmpty()) {

            userRegistrationDTO.setModulesList(new ArrayList<>(
                    Arrays.asList("673ae6fdc7de3b6aabc4d0d1", "673ae6fdc7de3b6aabc4d0d0", "673ae6fdc7de3b6aabc4d0cf",
                            "673ae6fdc7de3b6aabc4d0ce", "673ae6fdc7de3b6aabc4d0cc", "68302539a66102b563275b52",
                            "673ae6fdc7de3b6aabc4d0cb", "673ae6fdc7de3b6aabc4d0ca", "673c2b1dc7de3b6aabc4d0d2")));
        }

        DivisionLoginEntity div = divisionLoginRepository.save(userRegistrationDTO);

        if (userRegistrationDTO.getRoleId() == 7) {
            userRegistrationDTO.setPath("," + div.getId() + ",");
            userRegistrationDTO.setTrackDivisionId(div.getId());
        } else {
            userRegistrationDTO.setPath(userRegistrationDTO.getPath() + div.getId() + ",");
        }

        DivisionLoginEntity saved = divisionLoginRepository.save(userRegistrationDTO);
        divisionLoginTransactionRepository.save(toTransactionEntity(saved, saved.getId()));
        return saved;
    }

    private Optional<DivisionLoginEntity> getPathFromEmailId(String path) {
        return divisionLoginRepository.findByUserName(path);
    }

    @Override
    public DivisionLoginEntity getDivisionDetails(String userName, String password) {
        return divisionLoginRepository.findByUserNameAndPassword(userName, password);
    }

    @Override
    public DivisionLoginEntity updateDeviceList(String deviceList, String divisionId) {
        // Find the document by its ID
        DivisionLoginEntity entity = divisionLoginRepository.findById(divisionId).orElse(null);

        if (entity != null) {
            divisionLoginTransactionRepository.save(toTransactionEntity(entity, entity.getId()));
            String normalized = normalizeDeviceList(deviceList);
            Query query = new Query(Criteria.where("_id").is(new ObjectId(divisionId)));
            mongoTemplate.updateFirst(query,
                    new Update().set("device_list", normalized).set("last_modified", System.currentTimeMillis() / 1000),
                    DivisionLoginEntity.class);
            entity.setDeviceList(normalized);
        } else {
            log.warn("Document with ID {} not found for updateDeviceList", divisionId);
        }
        return entity;
    }

    @Override
    public List<DivisionLoginEntity> getTrackUserLogins() {
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository.findByRoleId(7);
        if (divisionLoginEntities.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        }
        return divisionLoginEntities;
    }

    @Override
    public List<DivisionLoginEntity> getDivParents(String divisionId) {
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository.findAll().stream()
                .filter(s -> s.getTrackDivisionId() != null).filter(s -> s.getTrackDivisionId().equals(divisionId))
                .collect(Collectors.toList());
        if (divisionLoginEntities.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        }
        return divisionLoginEntities;
    }

    @Override
    public DivisionLoginEntity updateDivisionLogin(DivisionLoginEntity divisionLogin) {
        DivisionLoginEntity entity = divisionLoginRepository.findById(divisionLogin.getId())
                .orElseThrow(() -> new RuntimeException("Division Login not found"));

        // Snapshot before update
        divisionLoginTransactionRepository.save(toTransactionEntity(entity, entity.getId()));

        // $set only the non-null incoming fields — never replaces the full document
        Query query = new Query(Criteria.where("_id").is(new ObjectId(entity.getId())));
        mongoTemplate.updateFirst(query, buildPartialUpdate(divisionLogin), DivisionLoginEntity.class);

        // Resolve effective values for cascade sync
        Integer roleId = divisionLogin.getRoleId() != null ? divisionLogin.getRoleId() : entity.getRoleId();
        String trackDivisionId = divisionLogin.getTrackDivisionId() != null ? divisionLogin.getTrackDivisionId()
                : entity.getTrackDivisionId();
        Boolean activeStatus = divisionLogin.getActiveStatus() != null ? divisionLogin.getActiveStatus()
                : entity.getActiveStatus();

        if (roleId != null && roleId == 7) {
            Query deviceQuery = new Query(Criteria.where("division_id").is(trackDivisionId));
            mongoTemplate.updateMulti(deviceQuery,
                    new Update().set("active_status", activeStatus).set("report_enable", activeStatus),
                    DeviceEntity.class);

            Query subUserQuery = new Query(Criteria.where("track_division_id").is(trackDivisionId));
            mongoTemplate.updateMulti(subUserQuery,
                    new Update().set("active_status", activeStatus).set("report_email_sent", activeStatus),
                    DivisionLoginEntity.class);
        } else {
            Query subUserQuery = new Query(Criteria.where("_id").is(entity.getId()));
            mongoTemplate.updateMulti(subUserQuery,
                    new Update().set("active_status", activeStatus).set("report_email_sent", activeStatus),
                    DivisionLoginEntity.class);
        }

        return divisionLoginRepository.findById(entity.getId()).orElse(entity);
    }

    private Update buildPartialUpdate(DivisionLoginEntity src) {
        Update update = new Update();
        if (src.getParentId() != null)
            update.set("parent_id", src.getParentId());
        if (src.getUserLoginId() != null)
            update.set("user_login_id", src.getUserLoginId());
        if (src.getSchoolId() != null)
            update.set("school_id", src.getSchoolId());
        if (src.getUserName() != null)
            update.set("user_name", src.getUserName());
        if (src.getName() != null)
            update.set("name", src.getName());
        if (src.getPassword() != null)
            update.set("password", src.getPassword());
        if (src.getMobileNo() != null)
            update.set("mobile_no", src.getMobileNo());
        if (src.getRoleId() != null)
            update.set("role_id", src.getRoleId());
        if (src.getDeptId() != null)
            update.set("dept_id", src.getDeptId());
        if (src.getCountyCode() != null)
            update.set("county_code", src.getCountyCode());
        if (src.getIsRailwayUser() != null)
            update.set("is_railway_user", src.getIsRailwayUser());
        if (src.getPath() != null)
            update.set("path", src.getPath());
        if (src.getDeviceList() != null)
            update.set("device_list", normalizeDeviceList(src.getDeviceList()));
        if (src.getCountryCode() != null)
            update.set("county", src.getCountryCode());
        if (src.getReportEmailId() != null)
            update.set("report_email_id", src.getReportEmailId());
        if (src.getReportEmailIdPassword() != null)
            update.set("report_email_password", src.getReportEmailIdPassword());
        if (src.getPoNo() != null)
            update.set("po_no", src.getPoNo());
        if (src.getPoEndDate() != null)
            update.set("po_end_date", src.getPoEndDate());
        if (src.getTrackDivisionId() != null)
            update.set("track_division_id", src.getTrackDivisionId());
        if (src.getLastModifiedBy() != null)
            update.set("last_modified_by", src.getLastModifiedBy());
        if (src.getShortName() != null)
            update.set("short_name", src.getShortName());
        if (src.getRole() != null)
            update.set("role", src.getRole());
        if (src.getReportEmailSent() != null)
            update.set("report_email_sent", src.getReportEmailSent());
        if (src.getEmailLoginPassword() != null)
            update.set("email_login_password", src.getEmailLoginPassword());
        if (src.getReportEnable() != null)
            update.set("report_enable", src.getReportEnable());
        if (src.getActiveStatus() != null)
            update.set("active_status", src.getActiveStatus());
        if (src.getWhatsappGroupName() != null)
            update.set("whatsapp_group_name", src.getWhatsappGroupName());
        if (src.getModulesList() != null)
            update.set("modules_list", src.getModulesList());
        if (src.getFcmTokenList() != null)
            update.set("fcm_token_list", src.getFcmTokenList());
        update.set("last_modified", System.currentTimeMillis() / 1000);
        return update;
    }

    private String normalizeDeviceList(String deviceList) {
        if (deviceList == null || deviceList.isBlank())
            return deviceList;
        StringBuilder sb = new StringBuilder(",");
        for (String part : deviceList.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty())
                sb.append(trimmed).append(",");
        }
        return sb.length() > 1 ? sb.toString() : deviceList;
    }

    @Override
    public List<FcmNotificationEntity> getFcmNotification(String divisionId) {
        List<Long> imeiList = deviceService.getAllDevicesOfDivision(divisionId).stream().map(DeviceDto::getImeiNo) // Extract
                // device_imei
                .filter(Objects::nonNull) // Optional: avoid nulls
                .collect(Collectors.toList());

        return fcmNotificationRepository.findByDeviceImeiIn(imeiList);
    }

    public List<DivisionLoginEntity> getSubUserFromDeviceNumber(String divId, String formattedOldDeviceNumber,
            int deviceNo) {
        log.info("getSubUserFromDeviceNumber{},{},{}", divId, formattedOldDeviceNumber, deviceNo);
        return divisionLoginRepository.findByDeviceListContainsWithDivId(divId, "," + formattedOldDeviceNumber + ",",
                "," + deviceNo + ",");

    }

    public Optional<DivisionLoginEntity> getDivisionFromWGroupName(String wGroupName) {
        log.info("Fetching division for group name: {}", wGroupName);
        DivisionLoginEntity entity = divisionLoginRepository.findFirstByRoleIdAndWhatsappGroupName(7, wGroupName);
        if (entity == null) {
            log.warn("No division found for group name: {}", wGroupName);
        }
        return Optional.ofNullable(entity);
    }

    public String getDivisionFromId(String _id) {
        if (_id == null)
            return "prime";

        return divisionLoginRepository.findById(_id).map(entity -> Optional.ofNullable(entity.getName()).orElse(_id))
                .orElse(_id);
    }

    @Override
    @Transactional
    public Optional<DivisionLoginEntity> addFcmToken(String divisionId, String token, String updatedBy) {
        divisionLoginRepository.findById(divisionId)
                .ifPresent(e -> divisionLoginTransactionRepository.save(toTransactionEntity(e, e.getId())));

        Query query = new Query(Criteria.where("_id").is(divisionId));
        Update update = new Update().addToSet("fcm_token_list", token).set("fcm_updated_at", LocalDateTime.now());

        try {
            log.info("Attempting atomic update for _id: {}", divisionId);
            DivisionLoginEntity updatedEntity = mongoTemplate.findAndModify(query, update,
                    FindAndModifyOptions.options().returnNew(true), DivisionLoginEntity.class);

            return Optional.ofNullable(updatedEntity);
        } catch (Exception e) {
            log.error("Failed to update entity with _id: {}", divisionId, e);
            throw new RuntimeException("Error updating entity", e);
        }
    }

    @Override
    public boolean removeFcmToken(String divisionId, String tokenToRemove) {
        Optional<DivisionLoginEntity> optional = divisionLoginRepository.findById(divisionId);
        if (optional.isPresent()) {
            DivisionLoginEntity entity = optional.get();
            Set<String> tokens = entity.getFcmTokenList();
            if (tokens != null && tokens.remove(tokenToRemove)) {
                divisionLoginTransactionRepository.save(toTransactionEntity(entity, entity.getId()));
                Query query = new Query(Criteria.where("_id").is(new ObjectId(divisionId)));
                mongoTemplate.updateFirst(query, new Update().set("fcm_token_list", tokens).set("last_modified",
                        System.currentTimeMillis() / 1000), DivisionLoginEntity.class);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<DivisionLoginEntity> getAdminLogins() {
        List<Integer> roleIds = Arrays.asList(19, 20);
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository.findByRoleIdIn(roleIds);

        if (divisionLoginEntities.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        }
        return divisionLoginEntities;
    }

    public Optional<DivisionLoginEntity> patchModulesList(String divisionId, List<String> modulesList,
            String modifiedBy) {
        return divisionLoginRepository.findById(divisionId).map(division -> {
            divisionLoginTransactionRepository.save(toTransactionEntity(division, division.getId()));
            Query query = new Query(Criteria.where("_id").is(new ObjectId(divisionId)));
            mongoTemplate.updateFirst(query, new Update().set("modules_list", modulesList)
                    .set("last_modified", System.currentTimeMillis() / 1000).set("last_modified_by", modifiedBy),
                    DivisionLoginEntity.class);
            division.setModulesList(modulesList);
            division.setLastModified(System.currentTimeMillis() / 1000);
            division.setLastModifiedBy(modifiedBy);
            return division;
        });
    }

    @Override
    public Optional<GpsTrackerReport> saveInspectionReport(GpsTrackerReport report) {
        return Optional.ofNullable(gpsTrackerReportRepository.save(report));

    }

    @Override
    public Optional<List<GpsTrackerReport>> getAllInspectionReport() {
        return Optional.ofNullable(gpsTrackerReportRepository.findAll());
    }

    @Override
    public Optional<List<GpsTrackerReport>> getDivisionInspectionReport(String divisionId) {
        return gpsTrackerReportRepository.findByDivisionId(divisionId);
    }

    @Override
    public Optional<GpsTrackerReport> addDeviceToReport(String id, List<DeviceTestReport> newDevices) {
        Optional<GpsTrackerReport> reportOpt = gpsTrackerReportRepository.findById(id);
        if (reportOpt.isPresent()) {
            GpsTrackerReport report = reportOpt.get();

            if (report.getDevices() == null) {
                report.setDevices(new ArrayList<>());
            }

            report.getDevices().addAll(newDevices);
            gpsTrackerReportRepository.save(report);
            return Optional.of(report);
        }
        return Optional.empty();
    }

    private DivisionLoginTransactionEntity toTransactionEntity(DivisionLoginEntity src, String masterId) {
        return DivisionLoginTransactionEntity.builder().masterId(masterId != null ? new ObjectId(masterId) : null)
                .parentId(src.getParentId()).userLoginId(src.getUserLoginId()).schoolId(src.getSchoolId())
                .userName(src.getUserName()).name(src.getName()).password(src.getPassword()).mobileNo(src.getMobileNo())
                .roleId(src.getRoleId()).deptId(src.getDeptId()).countyCode(src.getCountyCode())
                .isRailwayUser(src.getIsRailwayUser()).path(src.getPath()).deviceList(src.getDeviceList())
                .countryCode(src.getCountryCode()).reportEmailId(src.getReportEmailId())
                .reportEmailIdPassword(src.getReportEmailIdPassword()).poNo(src.getPoNo()).poEndDate(src.getPoEndDate())
                .trackDivisionId(src.getTrackDivisionId()).createdAt(src.getCreatedAt())
                .lastModified(src.getLastModified()).lastModifiedBy(src.getLastModifiedBy())
                .shortName(src.getShortName()).role(src.getRole()).reportEmailSent(src.getReportEmailSent())
                .emailLoginPassword(src.getEmailLoginPassword()).reportEnable(src.getReportEnable())
                .activeStatus(src.getActiveStatus()).whatsappGroupName(src.getWhatsappGroupName())
                .modulesList(src.getModulesList()).fcmTokenList(src.getFcmTokenList())
                .fcmUpdatedAt(src.getFcmUpdatedAt()).transactionAt(System.currentTimeMillis() / 1000)
                .ipAddress(resolveClientIp()).userAgent(resolveUserAgent()).build();
    }

    private String resolveClientIp() {
        try {
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
                ip = req.getHeader("X-Real-IP");
            if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
                ip = req.getRemoteAddr();
            if (ip != null && ip.contains(","))
                ip = ip.split(",")[0].trim();
            return ip;
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveUserAgent() {
        try {
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            return req.getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<EmailMasterEntity> getReportEmailMaster() {
        List<EmailMasterEntity> emails = emailMasterRepository.findAll();

        for (EmailMasterEntity email : emails) {
            List<DivisionLoginEntity> divisions = divisionLoginRepository.findByReportEmailId(email.getEmail());

            // extract only names
            List<String> divisionNames = divisions.stream().map(DivisionLoginEntity::getName) // get name
                    .collect(Collectors.toList());

            email.setDivisions(divisionNames); // store only names
            email.setDivisionsCount(divisionNames.size());
        }

        return emails;
    }

}