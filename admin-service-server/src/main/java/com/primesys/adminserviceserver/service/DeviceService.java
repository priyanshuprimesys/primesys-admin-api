package com.primesys.adminserviceserver.service;

import com.primesys.adminservicecommon.dto.DeviceDto;
import com.primesys.adminservicecommon.dto.DeviceExchangeDTO;
import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.entity.DeviceInfoMaster;
import com.primesys.adminserviceserver.request.DeviceRequest;
import com.primesys.adminserviceserver.request.RenewDeviceRequest;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface DeviceService {

    Optional<DeviceEntity> updateDevice(DeviceRequest device);

    Optional<FileUploadResultResponse> createDevice(List<List<String>> devices, DeviceRequest deviceRequest,
            int deviceStartSerialNo);

    List<DeviceDto> getAllDevicesOfDivision(String divisionID);

    long updateDeviceTypeIdAndShiftTypeInDevice(Long imei, int device_type, int shift_type);

    Object updateDeviceName(Long imei, String name);

    Long updateSosNumber(Long imei, List<String> sosNumbers);

    DeviceEntity getDevicesDetails(String deviceImei);

    Optional<DeviceEntity> updateDeviceImei(Long oldDeviceImei, Long newDeviceImei);

    String exchangeDevice(String oldDeviceId, String newDeviceId, String userLoginId);

    Integer renewDeviceDivisionWise(String divisionId, String userLoginId, Integer days);

    List<DeviceDto> getAllDevices();

    Optional<DeviceEntity> getDevice(DeviceRequest device);

    Optional<DeviceEntity> saveDevice(DeviceRequest device);

    Optional<List<DeviceEntity>> getAllDevicesWithDetailsOfDivision(String divisionId);

    Integer renewDevice(String deviceId, String userLoginId, Integer days);

    Integer renewDeviceRange(RenewDeviceRequest renewDeviceRequest);

    Page<DeviceExchangeDTO> getExchangeDevice(String userLoginId, Pageable pageable);

    Optional<FileUploadResultResponse> updateColumn(String col, String s, String updatedBy) throws IOException;

    DeviceInfoMaster getDeviceInfoByImei(Long imei);

    List<DeviceEntity> getAllDevicesWithDetails(String divisionId);
}
