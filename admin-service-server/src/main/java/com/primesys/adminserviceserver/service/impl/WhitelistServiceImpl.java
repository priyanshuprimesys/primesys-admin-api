package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.entity.PendingWhitelistEntity;
import com.primesys.adminservicemongodb.repository.DeviceRepository;
import com.primesys.adminservicemongodb.repository.PendingWhitelistRepository;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.ResourceNotFoundException;
import com.primesys.adminserviceserver.request.WhitelistRequest;
import com.primesys.adminserviceserver.service.WhitelistService;
import com.primesys.adminserviceserver.utility.WhitelistCommandBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhitelistServiceImpl implements WhitelistService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_PENDING, STATUS_COMPLETED);

    private final PendingWhitelistRepository pendingWhitelistRepository;
    private final DeviceRepository deviceRepository;

    @Override
    public List<PendingWhitelistEntity> getWhitelist(String status) {
        if (status != null && !status.isBlank()) {
            return pendingWhitelistRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        }
        return pendingWhitelistRepository.findByOrderByCreatedAtDesc();
    }

    @Override
    public List<PendingWhitelistEntity> createWhitelist(WhitelistRequest request) {
        if (request.getDeviceImei() == null) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString() + " : device imei is required");
        }

        DeviceEntity device = deviceRepository.findByDeviceImei(request.getDeviceImei());
        if (device == null) {
            throw new ResourceNotFoundException(
                    ErrorCode.NOT_FOUND.toString() + " : device not found for imei " + request.getDeviceImei());
        }

        // Resolve provider and display name from the device when not supplied on the request.
        String provider = (request.getSimProvider() != null && !request.getSimProvider().isBlank())
                ? request.getSimProvider() : device.getSimServiceProvider();
        String deviceName = (request.getDeviceName() != null && !request.getDeviceName().isBlank())
                ? request.getDeviceName() : device.buildDeviceName();

        long now = System.currentTimeMillis();
        List<PendingWhitelistEntity> created = new ArrayList<>();

        String fnCommand = WhitelistCommandBuilder.buildFn(provider, request.getFamilyNumbers());
        if (fnCommand != null) {
            created.add(savePending(request, device, deviceName, provider, WhitelistCommandBuilder.TYPE_FN, fnCommand,
                    now));
        }

        String sosCommand = WhitelistCommandBuilder.buildSos(provider, request.getSosNumbers());
        if (sosCommand != null) {
            created.add(savePending(request, device, deviceName, provider, WhitelistCommandBuilder.TYPE_SOS, sosCommand,
                    now));
        }

        return created;
    }

    @Override
    public PendingWhitelistEntity updateStatus(String id, String status, String updatedBy) {
        String normalizedStatus = validateStatus(status);
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid whitelist id: " + id);
        }

        PendingWhitelistEntity entity = pendingWhitelistRepository.findById(objectId).orElseThrow(
                () -> new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString() + " : whitelist entry " + id));

        entity.setStatus(normalizedStatus);
        entity.setUpdatedAt(System.currentTimeMillis());
        entity.setUpdatedBy(updatedBy);
        PendingWhitelistEntity saved = pendingWhitelistRepository.save(entity);
        log.info("whitelist status updated id={} imei={} type={} status={}", id, saved.getDeviceImei(),
                saved.getCommandType(), normalizedStatus);
        return saved;
    }

    @Override
    public List<PendingWhitelistEntity> updateStatusByDeviceImei(Long deviceImei, String status, String updatedBy) {
        if (deviceImei == null) {
            throw new IllegalArgumentException("device imei is required");
        }
        String normalizedStatus = validateStatus(status);

        List<PendingWhitelistEntity> entries = pendingWhitelistRepository.findByDeviceImei(deviceImei);
        if (entries.isEmpty()) {
            throw new ResourceNotFoundException(
                    ErrorCode.NOT_FOUND.toString() + " : no whitelist entries for imei " + deviceImei);
        }

        long now = System.currentTimeMillis();
        for (PendingWhitelistEntity entity : entries) {
            entity.setStatus(normalizedStatus);
            entity.setUpdatedAt(now);
            entity.setUpdatedBy(updatedBy);
        }
        List<PendingWhitelistEntity> saved = pendingWhitelistRepository.saveAll(entries);
        log.info("whitelist status updated by imei={} count={} status={}", deviceImei, saved.size(), normalizedStatus);
        return saved;
    }

    /** Validates and normalizes a status to upper-case; rejects anything outside the allowed set. */
    private String validateStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required");
        }
        String normalized = status.trim().toUpperCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed: " + ALLOWED_STATUSES);
        }
        return normalized;
    }

    private PendingWhitelistEntity savePending(WhitelistRequest request, DeviceEntity device, String deviceName,
            String provider, String commandType, String command, long now) {
        // keep one pending entry per device + command type (unique latest pending)
        List<PendingWhitelistEntity> existing = pendingWhitelistRepository
                .findByDeviceImeiAndCommandType(request.getDeviceImei(), commandType);
        if (!existing.isEmpty()) {
            List<PendingWhitelistEntity> stalePending = existing.stream()
                    .filter(e -> STATUS_PENDING.equalsIgnoreCase(e.getStatus())).toList();
            if (!stalePending.isEmpty()) {
                pendingWhitelistRepository.deleteAll(stalePending);
            }
        }

        Long parentId = request.getParentId() != null ? request.getParentId() : device.getParentId();

        PendingWhitelistEntity entity = PendingWhitelistEntity.builder().deviceImei(request.getDeviceImei())
                .deviceName(deviceName).commandType(commandType).command(command)
                .simProvider(WhitelistCommandBuilder.normalizeProvider(provider)).parentId(parentId)
                .loginName(request.getLoginName()).status(STATUS_PENDING).createdAt(now).build();

        PendingWhitelistEntity saved = pendingWhitelistRepository.save(entity);
        log.info("created pending whitelist imei={} type={} command={}", saved.getDeviceImei(), commandType, command);
        return saved;
    }
}
