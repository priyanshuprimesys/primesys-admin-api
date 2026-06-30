package com.primesys.adminserviceserver.service.impl;

import com.mongodb.client.result.UpdateResult;
import com.primesys.adminservicecommon.dto.DeviceDto;
import com.primesys.adminservicemongodb.util.DeviceNameUtil;
import com.primesys.adminservicecommon.dto.DeviceExchangeDTO;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.*;
import com.primesys.adminservicemongodb.model.Command;
import com.primesys.adminservicemongodb.model.Commands;
import com.primesys.adminservicemongodb.model.DevicePayment;
import com.primesys.adminservicemongodb.repository.*;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.ResourceNotFoundException;
import com.primesys.adminserviceserver.request.DeviceRequest;
import com.primesys.adminserviceserver.request.RenewDeviceRequest;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.service.DeviceService;
import com.primesys.adminserviceserver.service.DivisionLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceInfoMasterRepository deviceInfoMasterRepository;
    private final TodayLocationRepository todayLocationRepository;
    private final DevicePaymentTransactionRepository devicePaymentTransactionRepository;
    private final DeviceCommandHistoryRepository deviceCommandHistoryRepository;

    private final DivisionLoginRepository divisionLoginRepository;
    @Autowired
    @Lazy
    private DivisionLoginService divisionLoginService;

    private final MongoTemplate mongoTemplate;
    @Autowired
    private DeviceCommandServiceImpl deviceCommandServiceImpl;
    @Autowired
    private DeviceExchangeRepository deviceExchangeRepository;
    @Autowired
    private BeatRepository beatRepository;

    @Override
    public Optional<DeviceEntity> updateDevice(DeviceRequest device) {
        DeviceEntity existingDevice = deviceRepository.findByDeviceImei(device.getDeviceImei());
        if (Objects.nonNull(existingDevice)) {
            existingDevice.setDivisionId((device.getDivisionId()));
            existingDevice.setDeviceName(device.getDeviceName());
            existingDevice.setDeviceNo((device.getDeviceNo()));
            existingDevice.setSosNumbers(device.getSosNumbers());
            existingDevice.setDeviceSimNo(device.getDeviceSimNo());
            existingDevice.setDeviceSimImeiNo(device.getDeviceSimImeiNo());
            existingDevice.setDeviceSimImsiNo(device.getDeviceSimImsiNo());
            existingDevice.setReportTimeMargin(device.getReportTimeMargin());
            existingDevice.setReportDistMargin(device.getReportDistMargin());
            existingDevice.setShowGoogleAddress(device.isShowGoogleAddress());
            existingDevice.setReportAsIndependentRdps(device.isReportAsIndependentRdps());
            existingDevice.setDeviceTypeId(device.getDeviceTypeId());
            existingDevice.getDevicePayment().setPaymentRenewDate(device.getActivationDate());
            existingDevice.getDevicePayment().setExpiryDate(device.getPaymentDate());
            existingDevice.setTripWiseReport(device.isTripWiseReport());
            return Optional.of(deviceRepository.save(existingDevice));
        }
        DeviceEntity newDevice = new DeviceEntity();
        newDevice.setDivisionId((device.getDivisionId()));
        newDevice.setDeviceName(device.getDeviceName());
        newDevice.setDeviceNo((device.getDeviceNo()));
        newDevice.setSosNumbers(device.getSosNumbers());
        newDevice.setDeviceSimNo(device.getDeviceSimNo());
        newDevice.setDeviceSimImeiNo(device.getDeviceSimImeiNo());
        newDevice.setDeviceSimImsiNo(device.getDeviceSimImsiNo());
        newDevice.setReportTimeMargin(device.getReportTimeMargin());
        newDevice.setReportDistMargin(device.getReportDistMargin());
        newDevice.setShowGoogleAddress(device.isShowGoogleAddress());
        newDevice.setReportAsIndependentRdps(device.isReportAsIndependentRdps());
        newDevice.setDeviceTypeId(device.getDeviceTypeId());
        DevicePayment devicePayment = new DevicePayment();
        devicePayment.setPaymentPlanId(1);
        devicePayment.setPaymentRenewDate(device.getActivationDate());
        devicePayment.setExpiryDate(device.getPaymentDate());
        newDevice.setDevicePayment(devicePayment);
        newDevice.setTripWiseReport(Boolean.FALSE);

        return Optional.of(deviceRepository.save(newDevice));
    }

    @Override
    public Optional<DeviceEntity> getDevice(DeviceRequest device) {
        DeviceEntity existingDevice = deviceRepository.findByDeviceImei(device.getDeviceImei());
        return Optional.ofNullable(existingDevice);
    }

    @Override
    @Transactional
    public Optional<DeviceEntity> saveDevice(DeviceRequest device) {
        // Check for duplicate IMEI in another division
        Optional<DeviceEntity> existingByImei = Optional
                .ofNullable(deviceRepository.findByDeviceImei(device.getDeviceImei()));

        if (existingByImei.isPresent()) {
            DeviceEntity existingDevice = existingByImei.get();
            log.info("existingByImei: {}", existingDevice);

            if (!existingDevice.getId().equals(device.getId()) && existingDevice.getDivisionId() != null) {
                String message = ErrorCode.DEVICE_DUPLICATE_FAILED.toString() + device.getDeviceImei()
                        + "device is already associated with division id " + ", cannot update cross-division device.";
                throw new ResourceNotFoundException(message);
            }

            if (existingDevice.getDivisionId() == null) {
                device.setId(existingDevice.getId());
            }
        }

        // Optional<DeviceEntity> byId = null;
        // // Try to find existing device by ID
        // if (device.getId()!=null)
        // byId = deviceRepository.findById(device.getId());
        // DeviceEntity entity = byId.orElse(new DeviceEntity());

        Optional<DeviceEntity> byId = (device.getId() != null) ? deviceRepository.findById(device.getId())
                : Optional.empty();
        DeviceEntity entity = byId.orElse(new DeviceEntity());

        if (byId.isEmpty()) {
            // Only for new devices
            DevicePayment defaultPayment = new DevicePayment();
            defaultPayment.setDeviceImei(device.getDeviceImei());
            defaultPayment.setPaymentPlanId(1); // default plan
            defaultPayment.setExpiryDate(0);
            defaultPayment.setPaymentRenewDate(0);
            defaultPayment.setUpdatedBy(device.getUpdatedBy());
            entity.setDevicePayment(defaultPayment);
        }

        // Set/update all fields
        entity.setDeviceImei(device.getDeviceImei());
        entity.setDeviceName(device.getDeviceName());
        entity.setDivisionId(device.getDivisionId());
        entity.setDeviceNo(device.getDeviceNo());
        entity.setSosNumbers(device.getSosNumbers());
        entity.setDeviceSimNo(device.getDeviceSimNo());
        entity.setDeviceSimImeiNo(device.getDeviceSimImeiNo());
        entity.setDeviceSimImsiNo(device.getDeviceSimImsiNo());
        entity.setShowGoogleAddress(device.isShowGoogleAddress());
        entity.setReportAsIndependentRdps(device.isReportAsIndependentRdps());
        entity.setDeviceTypeId(device.getDeviceTypeId());
        entity.setReportTimeMargin(device.getReportTimeMargin());
        entity.setOnTrackMargin(device.getOnTrackMargin());
        entity.setReportDistMargin(device.getReportDistMargin());
        // entity.setActivationDate(System.currentTimeMillis() / 1000);o
        entity.setDeviceUserType(device.getDeviceUserType());
        entity.setTripWiseReport(device.isTripWiseReport());
        entity.setSimServiceProvider(device.getSimServiceProvider());
        entity.setDeviceVersion(normalizeDeviceVersion(device.getDeviceVersion()));
        entity.setTrackPids(Collections.emptyList());
        entity.setUpdatedBy(device.getUpdatedBy());
        entity.setUpdatedAt(System.currentTimeMillis() / 1000);
        entity.setShiftType(device.getShiftType());
        entity.setReportEnable(device.isReportEnable());
        entity.setActiveStatus(device.isActiveStatus());

        return Optional.of(deviceRepository.save(entity));
    }

    /** Treat empty/blank device version as null; otherwise keep the trimmed value (e.g. "PL200", "GK309"). */
    private static String normalizeDeviceVersion(String deviceVersion) {
        return (deviceVersion == null || deviceVersion.isBlank()) ? null : deviceVersion.trim();
    }

    /**
     * Replaces each entity's deviceName with the display name (name + "-" + deviceNo) so list endpoints that serialize
     * the raw entity return the combined name. Operates on detached read results only — these are never re-saved.
     */
    private static List<DeviceEntity> applyDisplayNames(List<DeviceEntity> devices) {
        devices.forEach(d -> d.setDeviceName(d.buildDeviceName()));
        return devices;
    }

    // @Override
    // public Optional<DeviceEntity> saveDevice(DeviceRequest device) {
    // Optional<DeviceEntity> existingDeviceOp = deviceRepository.findById(device.getId());
    // log.info("existingDevice--"+existingDeviceOp);
    //
    // if (existingDeviceOp.isEmpty()) {
    // log.info("11111111111111111");
    // DevicePayment payment = new DevicePayment();
    // payment.setDeviceImei(device.getDeviceImei());
    // payment.setPaymentPlanId(1);
    // payment.setExpiryDate(0);
    // payment.setPaymentRenewDate(0);
    // payment.setUpdatedBy(device.getUpdatedBy());
    // DeviceEntity existingDevice = new DeviceEntity();
    // existingDevice.setDeviceImei(device.getDeviceImei());
    // existingDevice.setDeviceName(device.getDeviceName());
    // existingDevice.setDivisionId(device.getDivisionId());
    // existingDevice.setDeviceNo(device.getDeviceNo());
    // existingDevice.setDeviceSimNo(device.getDeviceSimNo());
    // existingDevice.setDeviceSimImeiNo(device.getDeviceSimImeiNo());
    // existingDevice.setShowGoogleAddress(device.isShowGoogleAddress());
    // existingDevice.setReportAsIndependentRdps(device.isReportAsIndependentRdps());
    // existingDevice.setDeviceTypeId(device.getDeviceTypeId());
    // existingDevice.setReportTimeMargin(device.getReportTimeMargin());
    // existingDevice.setOnTrackMargin(device.getOnTrackMargin());
    // existingDevice.setReportDistMargin(device.getReportDistMargin());
    // existingDevice.setActivationDate(System.currentTimeMillis() / 1000);
    // existingDevice.setDeviceUserType(device.getDeviceUserType());
    // existingDevice.setTripWiseReport(device.isTripWiseReport());
    // existingDevice.setSimServiceProvider(device.getSimServiceProvider());
    // existingDevice.setTrackPids(Collections.emptyList());
    // existingDevice.setUpdatedBy(device.getUpdatedBy());
    // existingDevice.setUpdatedAt(System.currentTimeMillis() / 1000);
    // existingDevice.setShiftType(device.getShiftType());
    // existingDevice.setReportEnable(device.isReportEnable());
    //
    // return Optional.of(deviceRepository.save(existingDevice));
    // } else if (existingDeviceOp.isPresent()) {
    // log.info("22222");
    // DeviceEntity existingDevice = existingDeviceOp.get();
    //
    // existingDevice.setDeviceImei(device.getDeviceImei());
    // existingDevice.setDeviceName(device.getDeviceName());
    // existingDevice.setDivisionId(device.getDivisionId());
    // existingDevice.setDeviceNo(device.getDeviceNo());
    // existingDevice.setDeviceSimNo(device.getDeviceSimNo());
    // existingDevice.setDeviceSimImeiNo(device.getDeviceSimImeiNo());
    // existingDevice.setShowGoogleAddress(device.isShowGoogleAddress());
    // existingDevice.setReportAsIndependentRdps(device.isReportAsIndependentRdps());
    // existingDevice.setDeviceTypeId(device.getDeviceTypeId());
    // existingDevice.setReportTimeMargin(device.getReportTimeMargin());
    // existingDevice.setOnTrackMargin(device.getOnTrackMargin());
    // existingDevice.setReportDistMargin(device.getReportDistMargin());
    // existingDevice.setActivationDate(System.currentTimeMillis() / 1000);
    // existingDevice.setDeviceUserType(device.getDeviceUserType());
    // existingDevice.setTripWiseReport(device.isTripWiseReport());
    // existingDevice.setSimServiceProvider(device.getSimServiceProvider());
    // existingDevice.setTrackPids(Collections.emptyList());
    // existingDevice.setUpdatedBy(device.getUpdatedBy());
    // existingDevice.setUpdatedAt(System.currentTimeMillis() / 1000);
    // existingDevice.setShiftType(device.getShiftType());
    // existingDevice.setReportEnable(device.isReportEnable());
    // return Optional.of(deviceRepository.save(existingDevice));
    // } else {
    // throw new NotFoundException(ErrorCode.DEVICE_DUPLICATE_FAILED,
    // new Exception(device.getDeviceImei() + " device is already associated with division id "
    // + existingDevice.getDivisionId() + " you can not update Intra Division data."));
    //
    // }
    // }

    @Override
    public Optional<List<DeviceEntity>> getAllDevicesWithDetailsOfDivision(String divisionId) {
        final List<DeviceEntity> deviceEntityList = deviceRepository.findByDivisionId(divisionId);
        final Optional<DivisionLoginEntity> dLEntity = divisionLoginRepository.findById(divisionId);

        if (dLEntity.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        if (dLEntity.get().getRole().toString().equalsIgnoreCase("TRACK_USER")
                && CollectionUtils.isEmpty(deviceEntityList))
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        else if (dLEntity.get().getRole().toString().equalsIgnoreCase("TRACK_USER")) {
            return Optional.of(applyDisplayNames(
                    deviceEntityList.stream().filter(deviceEntity -> deviceEntity.getDeviceNo() != null)
                            .sorted(Comparator.comparing(DeviceEntity::getDeviceNo)).collect(Collectors.toList())));
        } else if (dLEntity.get().getRole().toString().equalsIgnoreCase("RAIL_SUB_USER")) {
            final List<DivisionLoginEntity> dLEntityList = divisionLoginRepository.findByPath(divisionId);
            String joined = dLEntityList.stream().map(DivisionLoginEntity::getDeviceList) // This will call
                    // person.getName()
                    .collect(Collectors.joining(", ")).replaceAll(" ", "").replaceAll(",,", ",");
            List<Integer> ints = Arrays.stream(joined.substring(1, joined.length() - 1).split(","))
                    .filter(e -> e.length() > 1).map(Integer::parseInt).sorted().collect(Collectors.toList());
            final List<DeviceEntity> deviceEntityRailList = deviceRepository
                    .findByNoTrackDivId((dLEntityList.get(0).getTrackDivisionId()), ints);
            // System.out.println(deviceEntityRailList);
            return Optional.of(applyDisplayNames(deviceEntityRailList.stream()
                    .sorted(Comparator.comparing(DeviceEntity::getDeviceNo)).collect(Collectors.toList())));
        }
        return Optional.empty();
    }

    @Override
    public List<DeviceEntity> getAllDevicesWithDetails(String divisionId) {
        /**
         * fetch division login entity if not division found then return with error of no devices found
         */
        final List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository
                .findValidDeviceLists(divisionId);
        if (divisionLoginEntities.isEmpty()) {
            throw new IllegalArgumentException("No devices found in division");
        }

        List<Integer> deviceNos = divisionLoginEntities.stream().map(DivisionLoginEntity::getDeviceList)
                .filter(Objects::nonNull).flatMap(s -> Arrays.stream(s.split(","))).filter(str -> !str.isBlank())
                .map(Integer::parseInt).toList();

        /// if division found with path
        /// if no devices found then return response with no devices found in division
        List<DeviceEntity> deviceEntities = deviceRepository.findByNoTrackDivId(divisionId, deviceNos);
        if (deviceEntities.isEmpty()) {
            throw new ResourceNotFoundException("No devices found in this division");
        }

        deviceEntities.sort(Comparator.comparing(DeviceEntity::getDeviceNo));
        return applyDisplayNames(deviceEntities);
    }

    @Override
    public Integer renewDevice(String deviceId, String userLoginId, Integer days) {
        Optional<DeviceEntity> existingDevice = deviceRepository.findById((deviceId));

        List<DeviceEntity> renewDevice = new ArrayList<>();
        List<DevicePayment> renewDevicePayment = new ArrayList<>();
        if (Objects.isNull(existingDevice) || existingDevice.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());

        Long currentTimeMillis = System.currentTimeMillis() / 1000;
        DevicePayment devicePayment = new DevicePayment();
        devicePayment.setExpiryDate(currentTimeMillis + (86400 * days));
        devicePayment.setPaymentRenewDate(currentTimeMillis);
        devicePayment.setPaymentPlanId(1);
        devicePayment.setUpdatedBy(userLoginId);

        existingDevice.get().setDevicePayment(devicePayment);
        renewDevice.add(existingDevice.get());

        List<DeviceEntity> renewDeviceCount = deviceRepository.saveAll(renewDevice);
        List<DevicePayment> d2 = devicePaymentTransactionRepository.saveAll(renewDevicePayment);

        return renewDeviceCount.size();
    }

    @Override
    public Integer renewDeviceRange(RenewDeviceRequest renewDeviceRequest) {

        // Validate the device list
        if (renewDeviceRequest.getDeviceList() == null || renewDeviceRequest.getDeviceList().isEmpty()) {
            throw new IllegalArgumentException("Device list cannot be null or empty.");
        }

        // Convert string IDs to ObjectId
        List<ObjectId> objectIds = renewDeviceRequest.getDeviceList().stream().map(ObjectId::new)
                .collect(Collectors.toList());

        // Fetch existing devices from the database
        List<DeviceEntity> existingDevices = deviceRepository.findByIdsIn(objectIds);

        if (existingDevices == null || existingDevices.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());

        // Prepare data for renewal
        List<DeviceEntity> renewDevices = new ArrayList<>();
        List<DevicePayment> renewDevicePayments = new ArrayList<>();
        Long currentTimeMillis = System.currentTimeMillis() / 1000;

        for (DeviceEntity device : existingDevices) {
            // Update device payment details
            DevicePayment devicePayment = new DevicePayment();
            devicePayment.setExpiryDate(currentTimeMillis + (86400L * renewDeviceRequest.getDays())); // Adding days
            devicePayment.setPaymentRenewDate(currentTimeMillis);
            devicePayment.setPaymentPlanId(1); // Example plan ID
            devicePayment.setUpdatedBy(renewDeviceRequest.getUserLoginId());
            // devicePayment.setDeviceId(device.getId());

            // Update the device entity
            device.setDevicePayment(devicePayment);
            device.setUpdatedBy(renewDeviceRequest.getUserLoginId());
            device.setUpdatedAt(currentTimeMillis);

            // Add to list for batch processing
            renewDevices.add(device);
            renewDevicePayments.add(devicePayment);
        }

        // Save updated devices and device payments
        List<DeviceEntity> updatedDevices = deviceRepository.saveAll(renewDevices);
        devicePaymentTransactionRepository.saveAll(renewDevicePayments);

        // Return the count of updated devices
        return updatedDevices.size();
    }

    @Override
    public Page<DeviceExchangeDTO> getExchangeDevice(String userLoginId, Pageable pageable) {
        Page<DeviceExchangeEntity> page = deviceExchangeRepository.findAll(pageable);

        if (page.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        }

        List<DeviceExchangeDTO> dtos = page.getContent().stream()
                .map(entity -> DeviceExchangeDTO.builder().id(entity.getId() != null ? entity.getId() : null)
                        .oldDeviceIMEI(entity.getOldDevice().getDeviceImei())
                        .oldDeviceName(entity.getOldDevice().getDeviceName())
                        .oldDeviceSimNo(entity.getOldDevice().getDeviceSimNo())
                        .oldDeviceSimIMEINo(entity.getOldDevice().getDeviceSimImeiNo())
                        .oldDeviceNo(entity.getOldDevice().getDeviceNo())
                        .oldDeviceTypeId(entity.getOldDevice().getDeviceTypeId())
                        .divisionId(entity.getOldDevice().getDivisionId()).exchangeBy(entity.getUpdatedBy())
                        .exchangeAt(entity.getTimestamp()).newDeviceIMEI(entity.getNewDevice().getDeviceImei())
                        .newDeviceName(entity.getNewDevice().getDeviceName())
                        .newDeviceSimNo(entity.getNewDevice().getDeviceSimNo())
                        .newDeviceSimIMEINo(entity.getNewDevice().getDeviceSimImeiNo())
                        .newDeviceNo(entity.getNewDevice().getDeviceNo())
                        .newDeviceTypeId(entity.getNewDevice().getDeviceTypeId()).build())
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());

    }

    @Override
    public Optional<FileUploadResultResponse> createDevice(List<List<String>> devices, DeviceRequest device,
            int deviceStartSerialNo) {
        int validRowLength = 4;
        DecimalFormat df = new DecimalFormat("000");

        int successRow = 0;
        int errorRow = 0;
        String errorMsg = validateDeviceSheet(devices);
        if (errorMsg.length() == 0) {
            for (int i = deviceStartSerialNo; i < devices.size() + (deviceStartSerialNo - 1); i++) {
                List<String> row = devices.get(i - (deviceStartSerialNo - 1));
                if (row.size() < validRowLength) {
                    errorRow++;
                    continue;
                }

                log.info("new ObjectId(device.getDivisionId())---" + device.getDivisionId());
                DeviceEntity deviceEntity = new DeviceEntity();
                deviceEntity.setDeviceName(DeviceNameUtil.format(row.get(0), df.format(i)));
                deviceEntity.setDeviceNo(i);
                deviceEntity.setDeviceImei(Long.parseLong(row.get(1)));
                deviceEntity.setDeviceSimNo(row.get(2));
                deviceEntity.setDeviceSimImeiNo(row.get(3));
                deviceEntity.setDivisionId((device.getDivisionId()));
                deviceEntity.setReportTimeMargin(device.getReportTimeMargin());
                deviceEntity.setReportDistMargin(device.getReportDistMargin());
                deviceEntity.setShowGoogleAddress(device.isShowGoogleAddress());
                deviceEntity.setReportAsIndependentRdps(device.isReportAsIndependentRdps());
                deviceEntity.setDeviceTypeId(device.getDeviceTypeId());
                DevicePayment devicePayment = new DevicePayment();
                devicePayment.setPaymentRenewDate(device.getActivationDate());
                devicePayment.setExpiryDate(device.getPaymentDate());
                deviceEntity.setDevicePayment(devicePayment);
                deviceEntity.setDeviceUserType(device.getDeviceUserType());
                deviceEntity.setTrackPids(new ArrayList());
                deviceEntity.setOnTrackMargin(100);
                deviceEntity.setActiveStatus(true);
                deviceEntity.setTripWiseReport(device.isTripWiseReport());
                deviceEntity.setDeviceVersion(normalizeDeviceVersion(device.getDeviceVersion()));

                DeviceEntity newDevice = deviceRepository.findByDeviceImei(Long.valueOf(row.get(1)));

                if (!Objects.isNull(newDevice)) {
                    if (newDevice.getDivisionId() == null) {
                        newDevice.setDevicePayment(devicePayment);
                        newDevice.setDeviceName(DeviceNameUtil.format(row.get(0), df.format(i)));
                        newDevice.setDeviceNo(i);
                        newDevice.setDeviceImei(Long.parseLong(row.get(1)));
                        newDevice.setDeviceSimNo(row.get(2));
                        newDevice.setDeviceSimImeiNo(row.get(3));
                        newDevice.setDivisionId((device.getDivisionId()));
                        newDevice.setReportTimeMargin(device.getReportTimeMargin());
                        newDevice.setReportDistMargin(device.getReportDistMargin());
                        newDevice.setShowGoogleAddress(device.isShowGoogleAddress());
                        newDevice.setReportAsIndependentRdps(device.isReportAsIndependentRdps());
                        newDevice.setDeviceTypeId(device.getDeviceTypeId());
                        newDevice.setDeviceUserType(device.getDeviceUserType());
                        newDevice.setOnTrackMargin(100);
                        newDevice.setTripWiseReport(Boolean.FALSE);
                        newDevice.setActiveStatus(true);
                        newDevice.setDeviceVersion(normalizeDeviceVersion(device.getDeviceVersion()));

                        newDevice.setTrackPids(new ArrayList());

                        deviceRepository.save(newDevice);
                    } else {

                        String message = ErrorCode.DEVICE_DUPLICATE_FAILED.toString() + newDevice.getDeviceImei()
                                + " device is already associated with division id "
                                + divisionLoginService.getDivisionFromId(newDevice.getDivisionId());

                        throw new ResourceNotFoundException(message);

                    }
                } else
                    deviceRepository.save(deviceEntity);
                successRow++;
            }
        } else {
            throw new ResourceNotFoundException(ErrorCode.DEVICE_DUPLICATE_FAILED.toString() + new Exception(errorMsg));
        }
        return Optional
                .of(FileUploadResultResponse.builder().validRecords(successRow).invalidRecords(errorRow).build());
    }

    private String validateDeviceSheet(List<List<String>> devices) {
        StringBuilder error = new StringBuilder();
        int validRowLength = 4;

        for (int i = 1; i < devices.size(); i++) {
            List<String> row = devices.get(i);
            if (row.size() < validRowLength) {
                error.append("\n Insufficient information of device row " + row.get(0));
            }
            DeviceEntity newDevice = deviceRepository.findByDeviceImei(Long.valueOf(row.get(1).trim()));
            if (!Objects.isNull(newDevice) && (newDevice.getDivisionId() != null)) {
                error.append("\n" + newDevice.getDeviceImei() + " device is already associated with division id "
                        + divisionLoginService.getDivisionFromId(newDevice.getDivisionId()));

            } else
                log.info("else---" + Long.valueOf(row.get(1)));
            // else if (!Objects.isNull(newDevice)&&(newDevice.getDivisionId() == null)) {
            //// deviceRepository.deleteById(newDevice.getId());
            //// error.append("\n"+newDevice.getDeviceImei() + " device is already on server");
            // }
        }
        return error.toString();

    }

    @Override
    public List<DeviceDto> getAllDevicesOfDivision(final String divisionID) {
        final List<DeviceEntity> deviceEntityList = deviceRepository.findByDivisionId(divisionID);
        final Optional<DivisionLoginEntity> dLEntity = divisionLoginRepository.findById(divisionID);

        // log.info("role_Name----" + dLEntity);
        if (dLEntity.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        if (dLEntity.get().getRole().toString().equalsIgnoreCase("TRACK_USER")
                && CollectionUtils.isEmpty(deviceEntityList))
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        else if (dLEntity.get().getRole().toString().equalsIgnoreCase("TRACK_USER")) {
            return deviceEntityList.stream().filter(deviceEntity -> deviceEntity.getDeviceNo() != null)
                    .sorted(Comparator.comparing(DeviceEntity::getDeviceNo))
                    .map(deviceEntity -> DeviceDto.builder().imeiNo(deviceEntity.getDeviceImei())
                            .simNo(deviceEntity.getDeviceSimNo()).showGoogleAddress(deviceEntity.getShowGoogleAddress())
                            .validDay(deviceEntity.getDevicePayment() == null ? -1
                                    : (int) ((deviceEntity.getDevicePayment().getExpiryDate()
                                            - (System.currentTimeMillis() / 1000)) / 86400))
                            .name(deviceEntity.buildDeviceName()).deviceId(deviceEntity.getId())
                            .sosNumbers(deviceEntity.getSosNumbers()).deviceUsertype(deviceEntity.getDeviceUserType())
                            .deviceNo(deviceEntity.getDeviceNo()).isDeviceConnected(deviceEntity.isConnected())
                            .deviceNo(deviceEntity.getDeviceNo())

                            .deviceTypeId(deviceEntity.getDeviceTypeId())
                            .simServiceProvider(deviceEntity.getSimServiceProvider()).build())

                    .collect(Collectors.toList());
        } else if (dLEntity.get().getRole().toString().equalsIgnoreCase("RAIL_SUB_USER")) {
            final List<DivisionLoginEntity> dLEntityList = divisionLoginRepository.findByPath(divisionID);
            String joined = dLEntityList.stream().map(DivisionLoginEntity::getDeviceList) // This will call
                    // person.getName()
                    .collect(Collectors.joining(", ")).replaceAll(" ", "").replaceAll(",,", ",");
            List<Integer> ints = Arrays.stream(joined.substring(1, joined.length() - 1).split(","))
                    .filter(e -> e.length() > 1).map(Integer::parseInt).sorted().collect(Collectors.toList());
            final List<DeviceEntity> deviceEntityRailList = deviceRepository
                    .findByNoTrackDivId((dLEntityList.get(0).getTrackDivisionId()), ints);
            // System.out.println(deviceEntityRailList);
            return deviceEntityRailList.stream().sorted(Comparator.comparing(DeviceEntity::getDeviceNo))
                    .map(deviceEntity -> DeviceDto.builder().imeiNo(deviceEntity.getDeviceImei())
                            .simNo(deviceEntity.getDeviceSimNo()).showGoogleAddress(deviceEntity.getShowGoogleAddress())
                            .validDay(deviceEntity.getDevicePayment() == null ? -1
                                    : (int) ((deviceEntity.getDevicePayment().getExpiryDate()
                                            - (System.currentTimeMillis() / 1000)) / 86400))
                            .deviceNo(deviceEntity.getDeviceNo()).name(deviceEntity.buildDeviceName())
                            .sosNumbers(deviceEntity.getSosNumbers()).deviceId(deviceEntity.getId())
                            .deviceUsertype(deviceEntity.getDeviceUserType()).deviceNo(deviceEntity.getDeviceNo())
                            .deviceTypeId(deviceEntity.getDeviceTypeId())
                            .simServiceProvider(deviceEntity.getSimServiceProvider()).build())
                    .toList();

        }

        return new ArrayList<>();
    }

    @Override
    public long updateDeviceTypeIdAndShiftTypeInDevice(Long imei, int device_type, int shift_type) {
        Query query = new Query(Criteria.where("device_imei").is(imei));
        Update update = new Update().set("device_type_id", device_type).set("shift_type", shift_type)
                .set("report_enable", true);
        return mongoTemplate.updateFirst(query, update, DeviceEntity.class).getModifiedCount();
    }

    @Override
    public Long updateDeviceName(Long imei, String name) {
        log.info("updateDeviceName called with imei {} and name {}", imei, name);
        DeviceEntity deviceEntity = deviceRepository.findByDeviceImei(imei);
        if (deviceEntity != null && deviceEntity.getDeviceNo() != null) {
            Query query = new Query(Criteria.where("device_imei").is(imei));
            Update update = new Update().set("device_name", name);
            return mongoTemplate.updateMulti(query, update, DeviceEntity.class).getModifiedCount();
        }
        return Long.valueOf(0);
    }

    @Override
    public Long updateSosNumber(Long imei, List<String> sosNumbers) {
        DeviceEntity deviceEntity = deviceRepository.findByDeviceImei(imei);
        if (deviceEntity == null) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        }
        Query query = new Query(Criteria.where("device_imei").is(imei));
        Update update = new Update().set("sos_numbers", sosNumbers);
        return mongoTemplate.updateMulti(query, update, DeviceEntity.class).getModifiedCount();
    }

    @Override
    public DeviceEntity getDevicesDetails(String deviceImei) {
        DeviceEntity existingDevice = deviceRepository.findByDeviceImei(Long.valueOf(deviceImei));
        if (Objects.isNull(existingDevice))
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());

        return existingDevice;

    }

    @Override
    public Optional<DeviceEntity> updateDeviceImei(Long oldDeviceImei, Long newDeviceImei) {
        DeviceEntity existingDevice = deviceRepository.findByDeviceImei(Long.valueOf(oldDeviceImei));
        DeviceEntity newDevice = deviceRepository.findByDeviceImei(Long.valueOf(newDeviceImei));

        if (Objects.isNull(existingDevice))
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());

        if (!Objects.isNull(newDevice))

            if (newDevice.getDivisionId() == null)
                deviceRepository.deleteById(newDevice.getId());
            else
                throw new ResourceNotFoundException(ErrorCode.DEVICE_DUPLICATE_FAILED.toString());

        existingDevice.setDeviceImei(Long.valueOf(newDeviceImei));
        DeviceEntity savedDevice = deviceRepository.save(existingDevice);
        if (savedDevice != null)
            updateDeviceExchangeReplaceBeat(oldDeviceImei, newDeviceImei);

        return Optional.of(savedDevice);
    }

    public void updateDeviceExchangeReplaceBeat(Long oldImei, Long newImei) {
        Query query = new Query(Criteria.where("device_imei").is(oldImei).and("active_status").is(true));
        Update update = new Update().set("device_imei", newImei);
        mongoTemplate.updateMulti(query, update, BeatEntity.class);
    }

    @Override
    public synchronized String exchangeDevice(String oldDeviceId, String newDeviceId, String userLoginId) {
        StringBuilder resultMsg = new StringBuilder();

        try {

            DeviceEntity oldDevice = deviceRepository.findById(oldDeviceId).get();
            DeviceEntity newDevice = deviceRepository.findById(newDeviceId).get();
            String formattedOldDeviceNumber = String.format("%03d", oldDevice.getDeviceNo());

            // Save device exchange history
            DeviceExchangeEntity exDevice = new DeviceExchangeEntity();
            oldDevice.setTrackPids(new ArrayList<>());
            newDevice.setTrackPids(new ArrayList<>());

            exDevice.setOldDevice(oldDevice);
            exDevice.setNewDevice(newDevice);
            exDevice.setTimestamp(System.currentTimeMillis() / 1000);
            exDevice.setUpdatedBy(userLoginId);
            log.info(String.valueOf(exDevice));
            deviceExchangeRepository.save(exDevice);

            newDevice.setDeviceName(
                    oldDevice.getDeviceName().replace("-" + formattedOldDeviceNumber, "-" + newDevice.getDeviceNo())
                            .replace("-" + oldDevice.getDeviceNo(), " - " + newDevice.getDeviceNo()));
            newDevice.setDeviceTypeId(oldDevice.getDeviceTypeId());
            newDevice.setReportEnable(Boolean.TRUE);
            newDevice.setReportDistMargin(oldDevice.getReportDistMargin());
            newDevice.setReportTimeMargin(oldDevice.getReportTimeMargin());
            newDevice.setOnTrackMargin(oldDevice.getOnTrackMargin());
            newDevice.setShiftType(oldDevice.getShiftType());

            Optional<DivisionLoginEntity> trackUser = divisionLoginRepository
                    .findById(String.valueOf(new ObjectId(oldDevice.getDivisionId())));
            List<DivisionLoginEntity> userList = divisionLoginRepository.findByDeviceListContainsWithDivId(
                    oldDevice.getDivisionId(), "," + formattedOldDeviceNumber + ",",
                    "," + oldDevice.getDeviceNo() + ",");
            log.info("---------userList -------------" + userList.size());
            String concatenatedDeviceNames = "Device";
            if (trackUser.isPresent())
                concatenatedDeviceNames = trackUser.get().getShortName() + "/"
                        + userList.stream().map(DivisionLoginEntity::getShortName) // Extract the name from each entity
                                .collect(Collectors.joining("/")); // Join names with "/"

            // Step 1 update device exchange
            if (deviceRepository.save(newDevice) != null) {
                oldDevice.setDeviceName(concatenatedDeviceNames + " - " + formattedOldDeviceNumber);
                oldDevice.setDeviceTypeId(0);
                oldDevice.setReportEnable(false);
                deviceRepository.save(oldDevice);
                resultMsg.append("\nTask 1: Device Name exchange completed.");
            } else
                resultMsg.append("\nError: Task 1: Device Name exchange have and error.Please look manually");

            // Step 2 update beat
            Optional<List<BeatEntity>> oldDeviceBeatInfo = beatRepository
                    .findByDeviceImeiAndActiveStatus(oldDevice.getDeviceImei(), true);
            updateActiveBeatsToFalse(oldDevice.getDeviceImei());
            updateActiveBeatsToFalse(newDevice.getDeviceImei());

            if (oldDeviceBeatInfo.isPresent() && oldDeviceBeatInfo.get().size() > 0) {
                List<BeatEntity> beatEntityList = oldDeviceBeatInfo.get();
                for (BeatEntity beat : beatEntityList) {
                    // Perform your operations on each beat
                    beat.setDeviceImei(newDevice.getDeviceImei());
                    if (beatRepository.save(beat) != null)
                        resultMsg.append("\nTask 2: Trip " + beat.getTripNo() + " added successfully.");
                }
            } else {
                resultMsg.append("\n Error: Task 2: No beat information found for the given device IMEI.");
            }

            // Step 3 update hierarchy
            if (userList.size() > 0)
                for (DivisionLoginEntity user : userList) {
                    if (user.getDeviceList() != null && !user.getDeviceList().isEmpty()) {
                        log.info(user.getName() + "---" + user.getDeviceList());

                        // Convert the string to a List of Integers
                        List<Integer> deviceList = Arrays.stream(user.getDeviceList().split(","))
                                .filter(str -> !str.isEmpty()) // Ensure it's not empty
                                .map(Integer::parseInt).collect(Collectors.toList());

                        log.info("deviceList --" + deviceList);
                        log.info("oldDevice.getDeviceNo() --" + oldDevice.getDeviceNo());

                        // Replace old with new
                        deviceList.remove(oldDevice.getDeviceNo());
                        log.info("deviceList after remove--" + deviceList);

                        deviceList.add(newDevice.getDeviceNo());
                        // user.setDeviceList("," + deviceList.stream().distinct().sorted().map(String::valueOf)
                        // .collect(Collectors.joining(",")));
                        user.setDeviceList("," + deviceList.stream().distinct().sorted().map(String::valueOf)
                                .collect(Collectors.joining(",")) + ",");

                        // log.info("divisionLoginRepository.save(user))" + "---" + divisionLoginRepository.save(user));
                        DivisionLoginEntity savedUser = divisionLoginRepository.save(user);
                        if (savedUser != null)
                            resultMsg.append("\nTask 3: In Hierarchy Device number  " + oldDevice.getDeviceNo()
                                    + "  replace by  " + newDevice.getDeviceNo() + " Successfully for section "
                                    + user.getUsername() + ".");

                    } else
                        resultMsg.append(
                                "\n Error: Task 3: In update Hierarchy got error; Device list is null for section "
                                        + user.getUsername() + ". Please check Hierarchy manually.");

                }
            else
                resultMsg.append("\n Error: Task 3: Hierarchy not found for old device no " + oldDevice.getDeviceNo()
                        + ". Please check Hierarchy manually.");

            // Step 4 Send command to new device
            if (oldDeviceBeatInfo.isPresent() && oldDeviceBeatInfo.get().size() > 0) {

                Map timePeriod = new HashMap<>();
                timePeriod.put(newDevice.getDeviceImei(), oldDeviceBeatInfo.get());
                String command = sendPeriodCommandToDevice(timePeriod, newDevice.getDivisionId());
                resultMsg.append("\nTask 4: " + command);
                resultMsg.append("\nTask 4: Command send Successfully to device - " + newDevice.getDeviceName()
                        + " and deviceImei- " + newDevice.getDeviceImei());

            } else {
                resultMsg.append("\n Error: Task 4: As Trip not found in old device - " + oldDevice.getDeviceNo()
                        + " so PERIOD Command could not send to New device no - " + newDevice.getDeviceNo() + "("
                        + newDevice.getDeviceImei() + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();

            resultMsg.append("\n Error: Exception got  as " + e.getMessage());
        }
        return resultMsg.toString();
    }

    private String sendPeriodCommandToDevice(Map<Long, List<BeatEntity>> groupedRecords, String divisionId) {
        List<DeviceCommandHistoryEntity> command = new ArrayList<>();
        log.info("sendPeriodCommandToDevice call");

        groupedRecords.forEach((deviceImei, records) -> {
            log.info("sendPeriodCommandToDevice " + deviceImei + "   " + records);

            if (records == null || records.isEmpty())
                return;

            // Use only first and last trip
            BeatEntity firstTrip = records.get(0);
            BeatEntity lastTrip = records.get(records.size() - 1);

            int buffer = 3600;

            int start = handleNegativeTime(Math.toIntExact(firstTrip.getStartTime()));
            int end = handleNegativeTime(Math.toIntExact(lastTrip.getEndTime()));

            int adjustedStart = Math.max(60, start - buffer);
            int adjustedEnd = Math.min(86399, end + buffer + 60);

            boolean isCrossDay = adjustedEnd < adjustedStart;

            if (!isCrossDay) {
                // Single-day command
                String time = convertSecondsToHHmm(adjustedStart) + "-" + convertSecondsToHHmm(adjustedEnd);
                for (int i = 0; i < 3; i++) {
                    DeviceCommandHistoryEntity commandEntity = new DeviceCommandHistoryEntity();
                    commandEntity.setDeviceName(firstTrip.getDevice_name());
                    commandEntity.setDeviceImei(deviceImei);
                    commandEntity.setLoginName("PrimeTrack");
                    commandEntity.setDivisionId(divisionId);
                    commandEntity.setCommand("PERIOD,1,1," + i + "," + time);
                    log.info("Command to be sent: " + commandEntity.getCommand());
                    command.add(commandEntity);
                }
            } else {
                // Multi-day command (crossing midnight)
                String time1 = convertSecondsToHHmm(adjustedStart) + "-23:59";
                String time2 = "00:01-" + convertSecondsToHHmm(adjustedEnd);

                for (int i = 0; i < 3; i++) {
                    DeviceCommandHistoryEntity commandEntity = new DeviceCommandHistoryEntity();
                    commandEntity.setDeviceName(firstTrip.getDevice_name());
                    commandEntity.setDeviceImei(deviceImei);
                    commandEntity.setLoginName("PrimeTrack");
                    commandEntity.setDivisionId(divisionId);
                    commandEntity.setCommand("PERIOD,1,1," + i + "," + time1 + "," + time2);
                    log.info("Command to be sent: " + commandEntity.getCommand());
                    command.add(commandEntity);
                }
            }
            deviceCommandServiceImpl.sendCommand(command);
        });
        return command.stream().map(DeviceCommandHistoryEntity::getCommand).collect(Collectors.joining("\n"));

    }

    public static int convertTimeToSeconds(String time) {
        // Split the time string into hours and minutes
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);

        // Convert hours and minutes to seconds
        return (hours * 3600) + (minutes * 60);
    }

    @Override
    public Integer renewDeviceDivisionWise(String divisionId, String userLoginId, Integer days) {
        List<DeviceEntity> existingDevice = deviceRepository.findByDivisionId((divisionId));
        List<DeviceEntity> renewDevice = new ArrayList<>();
        List<DevicePayment> renewDevicePayment = new ArrayList<>();
        if (Objects.isNull(existingDevice) || existingDevice.size() == 0)
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());

        Long currentTimeMillis = System.currentTimeMillis() / 1000;
        DevicePayment devicePayment = new DevicePayment();
        devicePayment.setExpiryDate(currentTimeMillis + (86400 * days));
        devicePayment.setPaymentRenewDate(currentTimeMillis);
        devicePayment.setPaymentPlanId(1);
        devicePayment.setUpdatedBy(userLoginId);

        for (DeviceEntity device : existingDevice) {
            devicePayment.setDeviceImei(device.getDeviceImei());
            device.setDevicePayment(devicePayment);
            renewDevice.add(device);
            renewDevicePayment.add(devicePayment);
        }
        List<DeviceEntity> renewDeviceCount = deviceRepository.saveAll(renewDevice);
        List<DevicePayment> d2 = devicePaymentTransactionRepository.saveAll(renewDevicePayment);

        return renewDeviceCount.size();
    }

    @Override
    public List<DeviceDto> getAllDevices() {
        final List<DeviceEntity> deviceEntityList = deviceRepository.findAll();
        final List<DivisionLoginEntity> divisionList = divisionLoginRepository.findAll();

        // log.info("role_Name----" + dLEntity);
        if (deviceEntityList.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());

        // System.out.println(deviceEntityRailList);
        return deviceEntityList.stream().filter(e -> e.getDeviceNo() != null)
                .sorted(Comparator.comparing(DeviceEntity::getDeviceNo))
                .map(deviceEntity -> DeviceDto.builder().imeiNo(deviceEntity.getDeviceImei())
                        .simNo(deviceEntity.getDeviceSimNo()).showGoogleAddress(deviceEntity.getShowGoogleAddress())
                        .validDay(deviceEntity.getDevicePayment() == null ? -1
                                : (int) ((deviceEntity.getDevicePayment().getExpiryDate()
                                        - (System.currentTimeMillis() / 1000)) / 86400))
                        .deviceNo(deviceEntity.getDeviceNo()).name(deviceEntity.buildDeviceName())
                        .deviceId(deviceEntity.getId()).deviceUsertype(deviceEntity.getDeviceUserType())
                        .sosNumbers(deviceEntity.getSosNumbers())
                        .simServiceProvider(deviceEntity.getSimServiceProvider())
                        .deviceSimImsiNo(deviceEntity.getDeviceSimImsiNo())
                        .deviceVersion(deviceEntity.getDeviceVersion()).deviceNo(deviceEntity.getDeviceNo())
                        .divisionId(deviceEntity.getDivisionId())
                        .divisionName(divisionList.stream()
                                .filter(e -> e.getId().equalsIgnoreCase(deviceEntity.getDivisionId())).findFirst()
                                .map(e -> e.getName()).orElse("Unknown"))
                        .isDeviceConnected(deviceEntity.isConnected()).deviceTypeId(deviceEntity.getDeviceTypeId())
                        .build())
                .toList();

    }

    // Check if the time interval crosses to the next day
    public static boolean isCrossDayInterval(int startTimeSeconds, int endTimeSeconds) {
        return endTimeSeconds < startTimeSeconds; // End time is less than start time, hence crosses midnight
    }

    // Method to find the split index where time crosses '01:00'
    public static int findSplitIndex(List<String[]> timeList) {
        for (int i = 0; i < timeList.size(); i++) {
            if (timeList.get(i)[0].compareTo("00:00") >= 0) { // Find where startTime >= '01:00'
                return i;
            }
        }
        return timeList.size(); // Return end of list if no split point is found
    }

    public static String convertSecondsToHHmm(int totalSeconds) {
        // log.info("convertSecondsToHHmm==totalSeconds=" + totalSeconds);

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        // log.info("convertSecondsToHHmm===" + hours + "----" + minutes);
        return String.format("%02d:%02d", hours, minutes);
    }

    // Method to find the minimum start time in a list of times
    public static String findMinStartTime(List<String[]> timeList) {
        return timeList.stream().map(arr -> arr[0]) // Get the start time
                .min(String::compareTo) // Find the minimum start time
                .orElse("No data"); // Return default value if empty
    }

    // Method to find the maximum end time in a list of times
    public static String findMaxEndTime(List<String[]> timeList) {
        return timeList.stream().map(arr -> arr[1]) // Get the end time
                .max(String::compareTo) // Find the maximum end time
                .orElse("No data"); // Return default value if empty
    }

    // Mock method to simulate handling negative time
    public static int handleNegativeTime(int time) {
        return (time >= 0) ? time : (24 * 3600 + time);
    }

    // Check if the time range falls under the current day or the next day
    // public static boolean isCurrentDayTime(String startTime, String endTime) {
    // // Assuming the times are formatted as "HH:mm"
    // return !(startTime.compareTo("00:00") >= 0 && startTime.compareTo("05:00") < 0);
    // }
    public static boolean isCurrentDayTime(String startTime, String endTime) {
        // Assuming the times are formatted as "HH:mm"
        String dayStartTime = "15:00";
        String dayEndTime = "23:59";

        // Check if the startTime falls within the day time period
        return (startTime.compareTo(dayStartTime) >= 0 && startTime.compareTo(dayEndTime) < 0);
    }

    public void updateActiveBeatsToFalse(Long imei) {
        Query query = new Query(Criteria.where("device_imei").is(imei));
        Update update = new Update().set("active_status", false);
        mongoTemplate.updateMulti(query, update, BeatEntity.class);
    }

    @Override
    public Optional<FileUploadResultResponse> updateColumn(String col, String path, String updatedBy)
            throws IOException {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0); // first sheet

        int updatedCount = 0;
        int notUpdatedCount = 0;

        int colIndex = ExcelUnlockedColumnsChecker.getColumnIndexByName(sheet, col);
        StringBuilder error = new StringBuilder();
        for (Row row : sheet) {
            if (row.getRowNum() == 0)
                continue; // skip header row

            Cell imeiCell = row.getCell(0);
            Cell updateCell = row.getCell(colIndex);

            if (imeiCell == null || updateCell == null) {
                notUpdatedCount++;
                continue;
            }

            long imei;
            try {
                imei = Long.parseLong(imeiCell.getStringCellValue());
            } catch (Exception e) {
                e.printStackTrace();
                notUpdatedCount++;
                continue;
            }
            log.info("imei:{}", imei);

            Object value;
            String cellStr = String.valueOf(row.getCell(colIndex)); // Helper to get typed value
            if (cellStr.equalsIgnoreCase("true") || cellStr.equalsIgnoreCase("false")) {
                value = Boolean.parseBoolean(cellStr); // Converts to actual boolean true/false
            } else if (colIndex > 1 && colIndex <= 6) {
                value = Integer.parseInt(cellStr); // Converts to actual boolean true/false

            } else {
                value = cellStr; // Use as String (fallback)
            }

            log.info("value:{}", value);

            Query query = new Query(Criteria.where("device_imei").is(imei));
            Update update = new Update().set(col, value).set("updated_by", updatedBy).set("updated_at",
                    System.currentTimeMillis() / 1000);
            UpdateResult result = mongoTemplate.updateFirst(query, update, DeviceEntity.class);
            log.info("query:{}", query);
            log.info("update:{}", update);

            if (result.getModifiedCount() > 0 || result.getMatchedCount() > 0) {
                updatedCount++;
            } else {
                notUpdatedCount++;
                error.append(imei);
                error.append(",");
            }
        }

        log.info("✅ Total Updated: " + updatedCount);
        log.info("❌ Not Updated (missing or unmatched): " + notUpdatedCount);

        workbook.close();
        fis.close();
        if (notUpdatedCount > 0)
            return Optional.of(FileUploadResultResponse.builder().validRecords(updatedCount)
                    .invalidRecords(notUpdatedCount).errorDescription("Not updated: " + error.toString()).build());

        return Optional.of(FileUploadResultResponse.builder().validRecords(updatedCount).invalidRecords(notUpdatedCount)
                .errorDescription("").build());
    }

    @Override
    public DeviceInfoMaster getDeviceInfoByImei(Long imei) {
        DeviceInfoMaster device = deviceInfoMasterRepository.findByDeviceImei(imei);
        TodayLocationEntity location = todayLocationRepository.findByDeviceImei(imei);
        if (location != null)
            device.setLocation(location);
        device.setCommands(buildCommandsFromHistory(imei));

        // device_version lives in the `devices` collection, not device_info_master — surface it so the Edit
        // form can pre-fill it.
        DeviceEntity deviceEntity = deviceRepository.findByDeviceImei(imei);
        if (deviceEntity != null && device.getDeviceInfo() != null)
            device.getDeviceInfo().setDeviceVersion(deviceEntity.getDeviceVersion());

        return device;
    }

    private Commands buildCommandsFromHistory(Long imei) {
        List<DeviceCommandHistoryEntity> history = deviceCommandHistoryRepository
                .findByDeviceImeiOrderByTimestampDesc(imei);
        Commands commands = new Commands();
        for (DeviceCommandHistoryEntity entry : history) {
            String cmd = entry.getCommand();
            if (cmd == null)
                continue;
            String upper = cmd.toUpperCase();
            boolean hasResponse = entry.getDeviceResponse() != null && !entry.getDeviceResponse().isEmpty();
            if (upper.startsWith("FN")) {
                if (commands.getLatestFnSet() == null)
                    commands.setLatestFnSet(toCommand(entry));
                if (hasResponse && commands.getLatestFn() == null)
                    commands.setLatestFn(toCommand(entry));
            } else if (upper.startsWith("SOS")) {
                if (commands.getLatestSosSet() == null)
                    commands.setLatestSosSet(toCommand(entry));
                if (hasResponse && commands.getLatestSos() == null)
                    commands.setLatestSos(toCommand(entry));
            } else if (upper.startsWith("HBT")) {
                if (commands.getLatestHbtSet() == null)
                    commands.setLatestHbtSet(toCommand(entry));
                if (hasResponse && commands.getLatestHbt() == null)
                    commands.setLatestHbt(toCommand(entry));
            } else if (upper.startsWith("TIMER")) {
                if (commands.getLatestTimerSet() == null)
                    commands.setLatestTimerSet(toCommand(entry));
                if (hasResponse && commands.getLatestTimer() == null)
                    commands.setLatestTimer(toCommand(entry));
            } else if (upper.startsWith("PERIOD")) {
                if (commands.getLatestPeriodSet() == null)
                    commands.setLatestPeriodSet(toCommand(entry));
                if (hasResponse && commands.getLatestPeriod() == null)
                    commands.setLatestPeriod(toCommand(entry));
            } else if (upper.startsWith("STATUS")) {
                if (commands.getLatestStatus() == null)
                    commands.setLatestStatus(toCommand(entry));
            } else if (upper.startsWith("PARAM")) {
                if (commands.getLatestParam() == null)
                    commands.setLatestParam(toCommand(entry));
            }
        }
        return commands;
    }

    private Command toCommand(DeviceCommandHistoryEntity entity) {
        return new Command(entity.getCommand(), entity.getTimestamp(), entity.getDeliveredMessage(),
                entity.getLoginName(), entity.getDeviceResponse(), entity.getDeviceResponseTime());
    }

}
