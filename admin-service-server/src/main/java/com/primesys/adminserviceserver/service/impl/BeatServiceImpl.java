package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicecommon.dto.DeviceBeatDto;
import com.primesys.adminservicemongodb.util.DeviceNameUtil;
import com.primesys.adminservicemongodb.model.BeatGroupByFileDTO;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.BeatEntity;
import com.primesys.adminservicemongodb.entity.DeviceCommandHistoryEntity;
import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.model.DeviceGroupDTO;
import com.primesys.adminservicemongodb.repository.BeatRepository;
import com.primesys.adminservicemongodb.repository.DeviceRepository;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.ResourceNotFoundException;
import com.primesys.adminserviceserver.request.DeviceBeatRequest;
import com.primesys.adminserviceserver.request.ManualBeatTrip;
import com.primesys.adminserviceserver.response.DeviceUploadResult;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.service.BeatService;
import com.primesys.adminserviceserver.service.DeviceService;
import com.primesys.adminserviceserver.service.DeviceTypeService;
import com.primesys.adminserviceserver.service.DivisionLoginService;
import com.primesys.adminserviceserver.utility.TimeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BeatServiceImpl implements BeatService {
    private static final int DEVICE_NAME = 0;
    private static final int DEVICE_NO = 1;
    private static final int SECTION_NAME = 2;
    private static final int DEVICE_TYPE = 3;
    // private static final int SHIFT_TYPE = 4;
    private static final int START_KM = 4;
    private static final int END_KM = 5;
    public static final String COLON = ":";
    private final BeatRepository beatRepository;
    private final MongoTemplate mongoTemplate;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final DeviceTypeService deviceTypeService;
    @Autowired
    private DeviceCommandServiceImpl deviceCommandServiceImpl;
    @Autowired
    @Lazy
    private DivisionLoginService divisionLoginService;

    public void updateActiveBeatsToFalse(Long imei, String updatedBy) {
        Query query = new Query(Criteria.where("device_imei").is(imei));
        Update update = new Update().set("active_status", false).set("updated_by", updatedBy).set("updated_at",
                System.currentTimeMillis() / 1000);
        mongoTemplate.updateMulti(query, update, BeatEntity.class);
    }

    public void updateActiveBeatsToFalse(String id, String updatedBy) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(id)));
        Update update = new Update().set("active_status", false).set("updated_by", "delete_for_update" + updatedBy);
        mongoTemplate.updateMulti(query, update, BeatEntity.class);
    }

    @Override
    public List<BeatEntity> createBeat(BeatEntity beatEntity, boolean isMultiple) {
        if (!isMultiple)
            updateActiveBeatsToFalse(beatEntity.getDeviceImei(), "delete_for_new_");
        Optional<BeatEntity> dLEntity = Optional.of(beatRepository.save(beatEntity));

        if (dLEntity.isEmpty())
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());

        if (!dLEntity.isEmpty()) {
            if (beatEntity.getDevice_name().length() > 0)
                deviceService.updateDeviceName(beatEntity.getDeviceImei(), beatEntity.getDevice_name());

            deviceService.updateDeviceTypeIdAndShiftTypeInDevice(beatEntity.getDeviceImei(),
                    beatEntity.getDeviceTypeId(), beatEntity.getShiftType());
            return dLEntity.stream().toList();

        }
        return new ArrayList<>();
    }

    public List<DeviceBeatDto> getDeviceBeat(final Long deviceImei) {
        final Optional<List<BeatEntity>> tripEntityList = beatRepository.findByDeviceImeiAndActiveStatus(deviceImei,
                true);

        if (tripEntityList.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        }

        return tripEntityList.get().stream()
                .map(tripEntity -> DeviceBeatDto.builder().beatId(tripEntity.getId()).tEndKm(tripEntity.getTripEndKm())
                        .activeStatus(tripEntity.getActiveStatus()).studentId(tripEntity.getStudentId())
                        .bEndTime(tripEntity.getBreakEndTime()).bStartTime(tripEntity.getBreakStartTime())
                        .startTime(tripEntity.getStartTime()).endTime(tripEntity.getEndTime())
                        .deviceImei(tripEntity.getDeviceImei()).deviceName(tripEntity.getDevice_name())
                        .deviceNo(tripEntity.getDeviceNo()).deviceTypeId(tripEntity.getDeviceTypeId())
                        .tripNo(tripEntity.getTripNo())

                        .deviceTypeId(tripEntity.getDeviceTypeId()).shiftType(tripEntity.getShiftType())
                        .sectionName(tripEntity.getSectionName()).tStartKm(tripEntity.getTripStartKm()).build())
                .collect(Collectors.toList());
    }

    // Simple mutable stats bucket — one per device per upload
    private static class DeviceStats {
        final int deviceNo;
        final long deviceImei;
        int tripsCreated = 0;
        int tripsSkipped = 0;
        int tripsDuplicate = 0;
        final List<Integer> createdTripNos = new ArrayList<>();

        DeviceStats(int deviceNo, long deviceImei) {
            this.deviceNo = deviceNo;
            this.deviceImei = deviceImei;
        }

        DeviceUploadResult toResult() {
            return DeviceUploadResult.builder().deviceNo(deviceNo).deviceImei(deviceImei).tripsCreated(tripsCreated)
                    .tripsSkipped(tripsSkipped).tripsDuplicate(tripsDuplicate).createdTripNos(createdTripNos).build();
        }
    }

    @Override
    public Optional<FileUploadResultResponse> createBeatDeviceNo(DeviceBeatRequest beatReq, List<List<String>> beats,
            boolean isMultiple, String fileName, boolean dryRun) {
        int successRow = 0;
        int errorRow = 0;
        List<BeatEntity> newRecords = new LinkedList<>();
        Map<Integer, DeviceStats> statsMap = new LinkedHashMap<>(); // insertion-ordered
        log.info("beats size={} dryRun={}", beats.size(), dryRun);

        // #2 — build IMEI lookup cache once; avoids N redundant DB queries for same device
        Map<Integer, Long> imeiCache = new HashMap<>();
        for (List<String> beat : beats) {
            try {
                int dn = (Double.valueOf(beat.get(DEVICE_NO)).intValue());
                imeiCache.computeIfAbsent(dn, k -> getDeviceImei(beatReq.getDivisionId(), k));
            } catch (Exception ignored) {
            }
        }

        // pre-deactivate every unique device once — skipped in dry-run
        if (!isMultiple && !dryRun) {
            Set<Long> deactivated = new HashSet<>();
            for (Long imei : imeiCache.values()) {
                if (imei > 0 && deactivated.add(imei))
                    updateActiveBeatsToFalse(imei, "delete_for_new_" + beatReq.getUpdatedBy());
            }
        }

        Set<Long> metaUpdated = new HashSet<>();
        Set<String> seenTripKeys = new HashSet<>();
        // global counter of non-N/A trips created; direction alternates on this, not on per-row loop index
        int[] globalTripSeq = { 0 };

        // A device's trips may be spread across MULTIPLE rows, so shift type must be decided
        // from the device's WHOLE pooled trip set — not one row at a time. Pool every non-N/A
        // (start,end) pair per device up front, then compute one shift type per device.
        Map<Integer, List<int[]>> deviceTripPairs = new HashMap<>();
        for (List<String> beat : beats) {
            int dn;
            try {
                dn = Double.valueOf(beat.get(DEVICE_NO)).intValue();
            } catch (Exception e) {
                continue;
            }
            List<int[]> pairs = deviceTripPairs.computeIfAbsent(dn, k -> new ArrayList<>());
            for (int k = 6; k + 1 < beat.size(); k += 2) {
                Integer s = parseTripSeconds(beat.get(k));
                Integer en = parseTripSeconds(beat.get(k + 1));
                if (s != null && en != null)
                    pairs.add(new int[] { s, en });
            }
        }
        Map<Integer, Integer> deviceShiftTypeMap = new HashMap<>();
        deviceTripPairs.forEach((dn, pairs) -> deviceShiftTypeMap.put(dn, determineDeviceShiftType(pairs)));

        for (int row = 0; row < beats.size(); row++) {
            List<String> beat = beats.get(row);
            int beatLength = beat.size() - 6;

            int deviceNo = (Double.valueOf(beat.get(DEVICE_NO)).intValue());
            long deviceImei = imeiCache.getOrDefault(deviceNo, 0L);
            log.info("row={} deviceNo={} deviceImei={}", row, deviceNo, deviceImei);

            if (deviceImei > 0) {
                statsMap.computeIfAbsent(deviceNo, k -> new DeviceStats(deviceNo, deviceImei));
                DeviceStats stats = statsMap.get(deviceNo);

                if (beatLength > 0) {
                    int shiftType;
                    int tShiftType = 1;
                    if (beatReq.getShiftType() == 0) {
                        // device-level shift type (pooled across all its rows); fall back to
                        // the per-row computation if this device somehow wasn't pooled.
                        shiftType = deviceShiftTypeMap.getOrDefault(deviceNo,
                                determineShiftType(LocalDate.now(), beat));
                        String fStart = null, fEnd = null;
                        for (int k = 6; k + 1 < beat.size(); k += 2) {
                            if (!isNaOrBlank(beat.get(k)) && !isNaOrBlank(beat.get(k + 1))) {
                                fStart = beat.get(k);
                                fEnd = beat.get(k + 1);
                                break;
                            }
                        }
                        tShiftType = (fStart != null) ? getTripShiftTypeId(fStart, fEnd) : 1;
                    } else {
                        shiftType = beatReq.getShiftType();
                        tShiftType = beatReq.getShiftType();
                    }

                    // #1 — actual non-N/A trip count for tripCount field
                    int actualTripCount = 0;
                    for (int k = 0; k < beatLength / 2; k++) {
                        if (!isNaOrBlank(beat.get(6 + k * 2)) && !isNaOrBlank(beat.get(7 + k * 2)))
                            actualTripCount++;
                    }

                    int deviceTimeTripCount = 0;
                    for (int i = 0; i < beatLength / 2; i++) {
                        int tripNo = i + 1;
                        String startTimeStr = beat.get(deviceTimeTripCount + 6);
                        String endTimeStr = beat.get(deviceTimeTripCount + 7);
                        deviceTimeTripCount += 2;

                        if (isNaOrBlank(startTimeStr) || isNaOrBlank(endTimeStr)) {
                            stats.tripsSkipped++;
                            continue;
                        }

                        String tripKey = deviceImei + ":" + tripNo;
                        if (!seenTripKeys.add(tripKey)) {
                            log.warn("Duplicate tripNo {} for device {} — skipped", tripNo, deviceImei);
                            stats.tripsDuplicate++;
                            continue;
                        }

                        // direction alternates on global sequential count of non-N/A trips,
                        // not on per-row loop index i — fixes wrong KM direction in multi-row uploads
                        boolean goingForward = (globalTripSeq[0] % 2 == 0);
                        globalTripSeq[0]++;
                        int startIdx = goingForward ? START_KM : END_KM;
                        int endIdx = goingForward ? END_KM : START_KM;

                        BeatEntity beatEntity = new BeatEntity();
                        beatEntity.setDeviceImei(deviceImei);
                        beatEntity.setSectionName(beat.get(SECTION_NAME));
                        beatEntity.setDeviceTypeId(deviceTypeService.getDeviceTypeId(beat.get(DEVICE_TYPE)));
                        beatEntity.setShiftType(shiftType);
                        beatEntity.setTripNo(beatReq.getTripNo() == 0 ? tripNo : beatReq.getTripNo());
                        beatEntity.setDeviceNo(deviceNo);
                        beatEntity.setActiveStatus(true);
                        beatEntity.setApprovedStatus(false);
                        beatEntity.setBreakStartTime(getSecFromTime(beatReq.getBstartTime()));
                        beatEntity.setBreakEndTime(getSecFromTime(beatReq.getBendTime()));
                        beatEntity.setTripStartKm(Double.valueOf(beat.get(startIdx)));
                        beatEntity.setTripEndKm(Double.valueOf(beat.get(endIdx)));
                        long[] times = getTime(startTimeStr, endTimeStr, shiftType);
                        Map<String, Long> resultTime = TimeCalculator.getTime(startTimeStr, endTimeStr);
                        beatEntity.setStartTime(times[0]);
                        beatEntity.setEndTime(times[1]);
                        beatEntity.setTStartTime(resultTime.get("startTime"));
                        beatEntity.setTripTime(resultTime.get("tripTime"));
                        beatEntity.setTEndTime(resultTime.get("endTime"));
                        beatEntity.setRefFileName(fileName);
                        beatEntity.setTShiftType(tShiftType);
                        beatEntity.setTripCount((double) actualTripCount);
                        beatEntity.setDevice_name(DeviceNameUtil.format(beat.get(DEVICE_NAME), deviceNo));
                        beatEntity.setCreatedAt(System.currentTimeMillis() / 1000);
                        beatEntity.setCreatedBy(beatReq.getUpdatedBy());
                        newRecords.add(beatEntity);
                        stats.tripsCreated++;
                        stats.createdTripNos.add(tripNo);
                    }

                    // #3 — metadata update once per device, skipped in dry-run
                    if (!dryRun && metaUpdated.add(deviceImei)) {
                        deviceService.updateDeviceTypeIdAndShiftTypeInDevice(deviceImei,
                                deviceTypeService.getDeviceTypeId(beat.get(DEVICE_TYPE)), shiftType);
                        if (beat.get(DEVICE_NAME).trim().length() > 0)
                            deviceService.updateDeviceName(deviceImei, beat.get(DEVICE_NAME));
                    }
                } else if (!dryRun && beat.get(DEVICE_NAME).trim().length() > 0 && !metaUpdated.contains(deviceImei)) {
                    deviceService.updateDeviceName(deviceImei, beat.get(DEVICE_NAME));
                }
                successRow++;
            } else {
                errorRow++;
            }
        }

        // save to DB only when not a dry-run
        if (!dryRun) {
            List<BeatEntity> beatResult = beatRepository.saveAll(newRecords);
            Map<Long, List<BeatEntity>> groupedRecords = beatResult.stream()
                    .collect(Collectors.groupingBy(BeatEntity::getDeviceImei, TreeMap::new,
                            Collectors.collectingAndThen(Collectors.toList(),
                                    list -> list.stream().sorted(Comparator.comparing(BeatEntity::getTripNo))
                                            .collect(Collectors.toList()))));
            if (beatReq.getSendAutoPeriodCommand())
                sendPeriodCommandToDevice(groupedRecords, beatReq.getDivisionId());
        }

        List<DeviceUploadResult> deviceResults = statsMap.values().stream().map(DeviceStats::toResult)
                .collect(Collectors.toList());

        int totalCreated = deviceResults.stream().mapToInt(DeviceUploadResult::getTripsCreated).sum();
        int totalSkipped = deviceResults.stream().mapToInt(DeviceUploadResult::getTripsSkipped).sum();
        int totalDuplicate = deviceResults.stream().mapToInt(DeviceUploadResult::getTripsDuplicate).sum();
        int totalRows = successRow + errorRow;

        StringBuilder sb = new StringBuilder();
        sb.append(totalRows).append(totalRows == 1 ? " row" : " rows").append(" processed — ");
        sb.append(statsMap.size()).append(" unique device").append(statsMap.size() == 1 ? "" : "s").append(": ");
        sb.append(totalCreated).append(totalCreated == 1 ? " trip" : " trips").append(" uploaded");
        if (totalSkipped > 0)
            sb.append(", ").append(totalSkipped).append(" N/A slot").append(totalSkipped == 1 ? "" : "s")
                    .append(" skipped");
        if (totalDuplicate > 0)
            sb.append(", ").append(totalDuplicate).append(" duplicate").append(totalDuplicate == 1 ? "" : "s")
                    .append(" skipped");
        if (errorRow > 0)
            sb.append(". ").append(errorRow).append(" row").append(errorRow == 1 ? "" : "s")
                    .append(" failed (device not found in system)");
        if (dryRun)
            sb.append(" [DRY RUN — nothing saved]");

        return Optional.of(FileUploadResultResponse.builder().validRecords(successRow).invalidRecords(errorRow)
                .totalTripsCreated(totalCreated).totalTripsSkipped(totalSkipped).totalTripsDuplicate(totalDuplicate)
                .dryRun(dryRun ? Boolean.TRUE : null).summary(sb.toString()).devices(deviceResults).build());
    }
    // @Override
    // public Optional<FileUploadResultResponse> createBeatDeviceNoTime(DeviceBeatRequest beatReq, List<List<String>>
    // beats,
    // boolean isMultiple, String fileName) {
    // int successRow = 0;
    // int errorRow = 0;
    // int days=30;
    // LocalDate scheduleDate = LocalDate.now();
    // UUID refFileId = UUID.randomUUID();
    //
    // List<BeatEntity> newRecords = new LinkedList<>();
    // // String errors = validateBeatFile(beats);
    // // if (StringUtils.isNotBlank(errors)) {
    // // return Optional.of(FileUploadResultResponse.builder().validRecords(successRow).invalidRecords(beats.size())
    // // .errorDescription(errors).build());
    // // }
    // log.info("beats size--" + beats.size());
    // for (int d = 0; d < days; d++) {
    // LocalDate dayDate = scheduleDate.plusDays(d);
    // for (int row = 0; row < beats.size(); row++) {
    // List<String> beat = beats.get(row);
    // log.info("beat size--" + beat.size());
    // int beatLength = beat.size() - 6;
    // log.info("beatLength size--" + beatLength);
    //
    // int tripNo = 0;
    // boolean toggle = true;
    // int deviceNo = (Double.valueOf(beat.get(DEVICE_NO)).intValue());
    // long deviceImei = getDeviceImei(beatReq.getDivisionId(), deviceNo);
    // log.info("deviceImei ----" + deviceImei);
    //
    // if (deviceImei > 0) {
    // log.info("beat -" + beat);
    //
    // if (beatLength > 0) {
    // if (!isMultiple)
    // updateActiveBeatsToFalse(deviceImei, "delete_for_new_" + beatReq.getUpdatedBy());
    //
    // int deviceTimeTripCount = 0;
    // int shiftType;
    // int tShiftType = 1;
    // if (beatReq.getShiftType() == 0) {
    // // shiftType = getShiftTypeId(beat.get(0 + 6), beat.get(beatLength + 5),
    // // deviceTypeService.getDeviceTypeId(beat.get(DEVICE_TYPE)));
    // shiftType = determineShiftType(scheduleDate,beat);
    // log.info("determineShiftType-----Trip:" + tripNo + " " + shiftType);
    //// tShiftType = getTripShiftTypeId(beat.get(0 + 6), beat.get(beatLength + 5));
    //// log.info("getTripShiftTypeId-----Trip:" + tripNo + " " + tShiftType);
    //
    // } else {
    // shiftType = beatReq.getShiftType();
    // tShiftType = beatReq.getShiftType();
    // }
    // String scheduleId = deviceNo + "_" + dayDate + "_" + shiftType;
    //
    // for (int i = 0; i < beatLength / 2; i++) {
    // tripNo++;
    //// int startIdx = toggle ? START_KM : END_KM;
    //// int endIdx = toggle ? END_KM : START_KM;
    // BeatEntity beatEntity = new BeatEntity();
    //
    // // Determine if this trip goes to next day
    // String[] startParts = beat.get(deviceTimeTripCount + 6).split(":");
    // String[] endParts = beat.get(deviceTimeTripCount + 7).split(":");
    // LocalDateTime startDT = dayDate.atTime(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]));
    // LocalDateTime endDT = dayDate.atTime(Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));
    // boolean nextDay = !endDT.isAfter(startDT);
    //
    // // Alternate direction and set KM for odd/even trips
    // String direction;
    // double startKm, endKm;
    //
    // double startKmCell = Double.valueOf(beat.get(START_KM)); // Start KM
    // double endKmCell = Double.valueOf(beat.get(END_KM)); // End KM
    //
    // if (i % 2 == 1) { // Odd trip
    // direction = "A→B";
    // startKm = startKmCell;
    // endKm = endKmCell;
    // } else { // Even trip
    // direction = "B→A";
    // startKm = endKmCell;
    // endKm = startKmCell;
    // }
    //
    // // beatEntity.setDeviceNo((int) Double.parseDouble(beat.get(DEVICE_NO)));
    //
    // beatEntity.setDeviceImei(deviceImei);
    // beatEntity.setSectionName(beat.get(SECTION_NAME));
    // beatEntity.setDeviceTypeId(deviceTypeService.getDeviceTypeId(beat.get(DEVICE_TYPE)));
    //
    // beatEntity.setShiftType(shiftType);
    // if (beatReq.getTripNo() == 0)
    // beatEntity.setTripNo(tripNo);
    // else
    // beatEntity.setTripNo(beatReq.getTripNo());
    // beatEntity.setDeviceNo(deviceNo);
    // beatEntity.setActiveStatus(true);
    // beatEntity.setApprovedStatus(false);
    // beatEntity.setBreakStartTime(getSecFromTime(beatReq.getBstartTime()));
    // beatEntity.setBreakEndTime(getSecFromTime(beatReq.getBendTime()));
    // beatEntity.setTripStartKm(startKm);
    // beatEntity.setTripEndKm(endKm);
    // long[] times = getTime(beat.get(deviceTimeTripCount + 6), beat.get(deviceTimeTripCount + 7),
    // shiftType);
    // Map<String, Long> resultTime = TimeCalculator.getTime(beat.get(deviceTimeTripCount + 6),
    // beat.get(deviceTimeTripCount + 7));
    // beatEntity.setStartTime(toEpoch(dayDate, beat.get(deviceTimeTripCount + 6), nextDay));
    // beatEntity.setEndTime(toEpoch(dayDate, beat.get(deviceTimeTripCount + 7), nextDay));
    // beatEntity.setTStart_time(resultTime.get("startTime"));
    // beatEntity.setTripTime(resultTime.get("tripTime"));
    // beatEntity.setTEndTime(resultTime.get("endTime"));
    // beatEntity.setRefFileName(fileName);
    // beatEntity.setTShiftType(tShiftType);
    // beatEntity.setScheduleDate(scheduleDate);
    // beatEntity.setScheduleId(scheduleId);
    // beatEntity.setDirection(direction);
    // beatEntity.setRefFileId(refFileId.toString());
    //
    // beatEntity.setDevice_name(beat.get(DEVICE_NAME) + "-" + deviceNo);
    // beatEntity.setCreatedAt(System.currentTimeMillis() / 1000);
    // beatEntity.setCreatedBy(beatReq.getUpdatedBy());
    // toggle = !toggle;
    // deviceTimeTripCount = deviceTimeTripCount + 2;
    // newRecords.add(beatEntity);
    // }
    //
    // deviceService.updateDeviceTypeIdAndShiftTypeInDevice(deviceImei,
    // deviceTypeService.getDeviceTypeId(beat.get(DEVICE_TYPE)), shiftType);
    //
    // }
    // if (beat.get(DEVICE_NAME).trim().length() > 0)
    // deviceService.updateDeviceName(deviceImei, beat.get(DEVICE_NAME));
    // successRow++;
    //
    // } else {
    // errorRow++;
    // }
    // }
    // }
    // List<BeatEntity> beatResult = beatRepository.saveAll(newRecords);
    //// Group records by deviceImei
    // Map<Long, List<BeatEntity>> groupedRecords = beatResult.stream()
    // .collect(Collectors.groupingBy(BeatEntity::getDeviceImei, // Group by deviceImei
    // TreeMap::new, // Use TreeMap to maintain sorted keys
    // Collectors.collectingAndThen(Collectors.toList(),
    // list -> list.stream().sorted(Comparator.comparing(BeatEntity::getTripNo)) // Sort by
    // // tripNo
    // // or Start
    // // time
    // .collect(Collectors.toList()))));
    // if (beatReq.getSendAutoPeriodCommand())
    // sendPeriodCommandToDevice(groupedRecords, beatReq.getDivisionId());
    //
    // return Optional
    // .of(FileUploadResultResponse.builder().validRecords(successRow).invalidRecords(errorRow).build());
    // }

    /**
     * Manual multi-trip add. Reuses every helper of createBeatDeviceNo (IMEI lookup, deactivation, shift-type
     * detection, duplicate/N-A skip, dry-run, period command, response shape). The one deliberate difference: each trip
     * carries its OWN start/end KM (no single-pair alternation), as sent by the manual-entry UI.
     */
    @Override
    public Optional<FileUploadResultResponse> createBeatManual(DeviceBeatRequest beatReq, boolean dryRun) {
        String fileName = "manual_beat_insert_" + System.currentTimeMillis();
        int deviceNo = beatReq.getDeviceNo();
        long deviceImei = getDeviceImei(beatReq.getDivisionId(), deviceNo);
        log.info("createBeatManual deviceNo={} deviceImei={} dryRun={}", deviceNo, deviceImei, dryRun);

        if (deviceImei <= 0) {
            return Optional.of(FileUploadResultResponse.builder().validRecords(0).invalidRecords(1)
                    .errorDescription("Device no " + deviceNo + " not found in division " + beatReq.getDivisionId())
                    .summary("1 row processed — device not found in system").build());
        }

        boolean isMultiple = Boolean.TRUE.equals(beatReq.getIsMultipleBeatPath());
        if (!isMultiple && !dryRun)
            updateActiveBeatsToFalse(deviceImei, "delete_for_new_" + beatReq.getUpdatedBy());

        // UI sends the numeric device-type id ("2"); file upload sends the name ("Patrolman"). Accept both.
        int deviceTypeId = resolveDeviceTypeId(beatReq.getDeviceTypeId());

        // honor approvedStatus from request (defaults to false when absent)
        boolean approvedStatus = Boolean.TRUE.equals(beatReq.getApprovedStatus());
        // strip any trailing "-<deviceNo>" already on the incoming name so the suffix doesn't compound on re-submits
        String baseName = stripDeviceNoSuffix(beatReq.getDeviceName(), deviceNo);

        DeviceStats stats = new DeviceStats(deviceNo, deviceImei);
        List<ManualBeatTrip> trips = beatReq.getTrips() == null ? new ArrayList<>() : beatReq.getTrips();

        // shift type via the SAME determineShiftType — fed a synthetic row (6 leading cols + time pairs)
        List<String> shiftRow = new ArrayList<>(Collections.nCopies(6, ""));
        for (ManualBeatTrip t : trips) {
            shiftRow.add(t.getStartTime());
            shiftRow.add(t.getEndTime());
        }
        int shiftType = (beatReq.getShiftType() == 0) ? determineShiftType(LocalDate.now(), shiftRow)
                : beatReq.getShiftType();

        int actualTripCount = (int) trips.stream()
                .filter(t -> !isNaOrBlank(t.getStartTime()) && !isNaOrBlank(t.getEndTime())).count();

        List<BeatEntity> newRecords = new ArrayList<>();
        Set<String> seenTripKeys = new HashSet<>();
        int tripNo = 0;
        for (ManualBeatTrip t : trips) {
            tripNo++;
            if (isNaOrBlank(t.getStartTime()) || isNaOrBlank(t.getEndTime())) {
                stats.tripsSkipped++;
                continue;
            }
            if (!seenTripKeys.add(deviceImei + ":" + tripNo)) {
                log.warn("Duplicate tripNo {} for device {} — skipped", tripNo, deviceImei);
                stats.tripsDuplicate++;
                continue;
            }

            int tShiftType = (beatReq.getShiftType() == 0) ? getTripShiftTypeId(t.getStartTime(), t.getEndTime())
                    : beatReq.getShiftType();
            long[] times = getTime(t.getStartTime(), t.getEndTime(), shiftType);
            Map<String, Long> resultTime = TimeCalculator.getTime(t.getStartTime(), t.getEndTime());

            BeatEntity b = new BeatEntity();
            b.setDeviceImei(deviceImei);
            b.setSectionName(beatReq.getSectionName());
            b.setDeviceTypeId(deviceTypeId);
            b.setShiftType(shiftType);
            b.setTripNo(beatReq.getTripNo() == 0 ? tripNo : beatReq.getTripNo());
            b.setDeviceNo(deviceNo);
            b.setActiveStatus(true);
            b.setApprovedStatus(approvedStatus);
            b.setBreakStartTime(getSecFromTime(beatReq.getBstartTime()));
            b.setBreakEndTime(getSecFromTime(beatReq.getBendTime()));
            b.setTripStartKm(Double.valueOf(t.getStartKm())); // per-trip KM, no alternation
            b.setTripEndKm(Double.valueOf(t.getEndKm()));
            b.setStartTime(times[0]);
            b.setEndTime(times[1]);
            b.setTStartTime(resultTime.get("startTime"));
            b.setTripTime(resultTime.get("tripTime"));
            b.setTEndTime(resultTime.get("endTime"));
            b.setRefFileName(fileName);
            b.setTShiftType(tShiftType);
            b.setTripCount((double) actualTripCount);
            b.setDevice_name(DeviceNameUtil.format(baseName, deviceNo));
            b.setCreatedAt(System.currentTimeMillis() / 1000);
            b.setCreatedBy(beatReq.getUpdatedBy());
            newRecords.add(b);
            stats.tripsCreated++;
            stats.createdTripNos.add(tripNo);
        }

        if (!dryRun) {
            deviceService.updateDeviceTypeIdAndShiftTypeInDevice(deviceImei, deviceTypeId, shiftType);
            if (!baseName.isEmpty())
                deviceService.updateDeviceName(deviceImei, baseName);

            List<BeatEntity> saved = beatRepository.saveAll(newRecords);
            Map<Long, List<BeatEntity>> grouped = saved.stream()
                    .collect(Collectors.groupingBy(BeatEntity::getDeviceImei, TreeMap::new,
                            Collectors.collectingAndThen(Collectors.toList(),
                                    l -> l.stream().sorted(Comparator.comparing(BeatEntity::getTripNo))
                                            .collect(Collectors.toList()))));
            if (beatReq.getSendAutoPeriodCommand())
                sendPeriodCommandToDevice(grouped, beatReq.getDivisionId());
        }

        DeviceUploadResult dr = stats.toResult();
        StringBuilder sb = new StringBuilder();
        sb.append("1 row processed — 1 device: ").append(dr.getTripsCreated())
                .append(dr.getTripsCreated() == 1 ? " trip" : " trips").append(" uploaded");
        if (dr.getTripsSkipped() > 0)
            sb.append(", ").append(dr.getTripsSkipped()).append(" N/A slot")
                    .append(dr.getTripsSkipped() == 1 ? "" : "s").append(" skipped");
        if (dr.getTripsDuplicate() > 0)
            sb.append(", ").append(dr.getTripsDuplicate()).append(" duplicate")
                    .append(dr.getTripsDuplicate() == 1 ? "" : "s").append(" skipped");
        if (dryRun)
            sb.append(" [DRY RUN — nothing saved]");

        return Optional.of(FileUploadResultResponse.builder().validRecords(1).invalidRecords(0)
                .totalTripsCreated(dr.getTripsCreated()).totalTripsSkipped(dr.getTripsSkipped())
                .totalTripsDuplicate(dr.getTripsDuplicate()).dryRun(dryRun ? Boolean.TRUE : null).summary(sb.toString())
                .devices(List.of(dr)).build());
    }

    @Override
    public Optional<FileUploadResultResponse> createBeatHourly(DeviceBeatRequest beatReq, List<List<String>> beats,
            boolean isMultiple, String fileName) {
        int successRow = 0;
        int errorRow = 0;
        int days = 30;
        LocalDate baseDate = LocalDate.now();
        UUID refFileId = UUID.randomUUID();

        List<BeatEntity> newRecords = new ArrayList<>();
        log.info("beats size = {}", beats.size());

        for (int d = 0; d < days; d++) {
            LocalDate dayDate = baseDate.plusDays(d);

            for (List<String> beat : beats) {
                int beatLength = beat.size() - 6;
                if (beatLength <= 0) {
                    errorRow++;
                    continue;
                }

                int deviceNo = (int) Double.parseDouble(beat.get(DEVICE_NO));
                long deviceImei = getDeviceImei(beatReq.getDivisionId(), deviceNo);

                if (deviceImei <= 0) {
                    errorRow++;
                    continue;
                }

                if (!isMultiple) {
                    updateActiveBeatsToFalse(deviceImei, "delete_for_new_" + beatReq.getUpdatedBy());
                }

                int shiftType = (beatReq.getShiftType() == 0) ? determineShiftType(baseDate, beat)
                        : beatReq.getShiftType();
                int tShiftType = shiftType;

                String scheduleId = deviceNo + "_" + dayDate + "_" + shiftType;

                double startKmCell = Double.parseDouble(beat.get(START_KM));
                double endKmCell = Double.parseDouble(beat.get(END_KM));

                int deviceTimeTripCount = 0;
                for (int i = 0, tripNo = 1; i < beatLength / 2; i++, tripNo++) {
                    String startTimeStr = beat.get(deviceTimeTripCount + 6);
                    String endTimeStr = beat.get(deviceTimeTripCount + 7);

                    LocalDateTime startDT = parseTime(dayDate, startTimeStr);
                    LocalDateTime endDT = parseTime(dayDate, endTimeStr);
                    boolean nextDay = !endDT.isAfter(startDT);

                    // Direction & KM mapping
                    String direction = (i % 2 == 1) ? "A→B" : "B→A";
                    double tripStartKm = (i % 2 == 1) ? startKmCell : endKmCell;
                    double tripEndKm = (i % 2 == 1) ? endKmCell : startKmCell;

                    Map<String, Long> resultTime = TimeCalculator.getTime(startTimeStr, endTimeStr);

                    BeatEntity beatEntity = buildBeatEntity(beat, beatReq, deviceImei, deviceNo, fileName, refFileId,
                            tripNo, shiftType, tShiftType, dayDate, scheduleId, startTimeStr, endTimeStr, nextDay,
                            tripStartKm, tripEndKm, direction, resultTime);

                    newRecords.add(beatEntity);
                    deviceTimeTripCount += 2;
                }

                deviceService.updateDeviceTypeIdAndShiftTypeInDevice(deviceImei,
                        deviceTypeService.getDeviceTypeId(beat.get(DEVICE_TYPE)), shiftType);

                if (!beat.get(DEVICE_NAME).trim().isEmpty()) {
                    deviceService.updateDeviceName(deviceImei, beat.get(DEVICE_NAME));
                }
                successRow++;
            }
        }

        List<BeatEntity> beatResult = beatRepository.saveAll(newRecords);

        // Group by deviceImei & sort by tripNo
        Map<Long, List<BeatEntity>> groupedRecords = beatResult.stream()
                .collect(Collectors.groupingBy(BeatEntity::getDeviceImei, TreeMap::new,
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream().sorted(Comparator.comparing(BeatEntity::getTripNo)).toList())));

        if (beatReq.getSendAutoPeriodCommand()) {
            sendPeriodCommandToDevice(groupedRecords, beatReq.getDivisionId());
        }

        return Optional.of(FileUploadResultResponse.builder().validRecords(successRow / days)
                .invalidRecords(errorRow / days).build());
    }

    @Override
    public Optional<Integer> deleteBeataApprovalFile(String refFileName, String updatedBy) {
        Optional<List<BeatEntity>> list = beatRepository.findByRefFileName(refFileName);

        if (list.isEmpty() || list.get().isEmpty()) {
            return Optional.empty();
        }

        // delete all
        beatRepository.deleteByRefFileName(refFileName);

        // return the first deleted object for reference
        return Optional.of(list.get().size());
    }

    /**
     * Converts a date + time string to epoch seconds (handles rollover and IST).
     */
    private static long toEpoch(LocalDate scheduleDate, String timeStr, boolean nextDay) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        LocalDateTime dateTime = scheduleDate.atTime(hour, minute);
        if (nextDay) {
            dateTime = dateTime.plusDays(1);
        }
        return dateTime.toEpochSecond(ZoneOffset.ofHoursMinutes(5, 30)); // IST
    }

    /**
     * Parses a "HH:mm" string into LocalDateTime.
     */
    private static LocalDateTime parseTime(LocalDate date, String timeStr) {
        String[] parts = timeStr.split(":");
        return date.atTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    /**
     * Builds a BeatEntity instance with all required fields.
     */
    private BeatEntity buildBeatEntity(List<String> beat, DeviceBeatRequest beatReq, long deviceImei, int deviceNo,
            String fileName, UUID refFileId, int tripNo, int shiftType, int tShiftType, LocalDate dayDate,
            String scheduleId, String startTimeStr, String endTimeStr, boolean nextDay, double tripStartKm,
            double tripEndKm, String direction, Map<String, Long> resultTime) {
        return BeatEntity.builder().deviceImei(deviceImei).sectionName(beat.get(SECTION_NAME))
                .deviceTypeId(deviceTypeService.getDeviceTypeId(beat.get(DEVICE_TYPE))).shiftType(shiftType)
                .tripNo((beatReq.getTripNo() == 0) ? tripNo : beatReq.getTripNo()).deviceNo(deviceNo).activeStatus(true)
                .approvedStatus(false).breakStartTime(getSecFromTime(beatReq.getBstartTime()))
                .breakEndTime(getSecFromTime(beatReq.getBendTime())).tripStartKm(tripStartKm).tripEndKm(tripEndKm)
                .startTime(toEpoch(dayDate, startTimeStr, nextDay)).endTime(toEpoch(dayDate, endTimeStr, nextDay))
                .tStartTime(resultTime.get("startTime")).tEndTime(resultTime.get("endTime")).isHourly(true)
                .tripTime(resultTime.get("tripTime")).refFileName(fileName).tShiftType(tShiftType).scheduleDate(dayDate)
                .scheduleId(scheduleId).direction(direction).refFileId(refFileId.toString())
                .device_name(DeviceNameUtil.format(beat.get(DEVICE_NAME), deviceNo))
                .createdAt(System.currentTimeMillis() / 1000).createdBy(beatReq.getUpdatedBy()).build();
    }

    // private static long toEpoch(LocalDate scheduleDate, String timeStr, boolean nextDay) {
    // String[] parts = timeStr.split(":");
    // int hour = Integer.parseInt(parts[0]);
    // int minute = Integer.parseInt(parts[1]);
    //
    // LocalDateTime dateTime = scheduleDate.atTime(hour, minute);
    // if (nextDay) {
    // dateTime = dateTime.plusDays(1);
    // }
    //
    // // IST (GMT+5:30)
    // ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
    // return dateTime.toEpochSecond(istOffset);
    // }

    /**
     * Parse a beat time string ("HH:mm") to seconds-of-day. Returns null for N/A / blank / unparseable values so they
     * can be excluded from the pooled trip set.
     */
    private Integer parseTripSeconds(String s) {
        if (isNaOrBlank(s))
            return null;
        try {
            String[] p = s.trim().split(":");
            int h = Integer.parseInt(p[0].trim());
            int m = p.length > 1 ? Integer.parseInt(p[1].trim()) : 0;
            return h * 3600 + m * 60;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Device-level shift type from the device's WHOLE pooled trip set (all rows combined).
     * <p>
     * The real shift start is the trip start that follows the largest idle gap on the 24h clock (so morning trips like
     * 06:00 don't get mistaken for the start of the day). Every trip is then rolled forward from that start; if the
     * latest end lands on the next day the shift crosses midnight → type 2, otherwise type 1.
     * <p>
     * Example: pooled starts 20:00,21:00,…,06:00 → biggest gap is 07:00→20:00, so the shift starts at 20:00; the
     * 06:00–07:00 trip rolls to next-day 07:00 → crosses midnight → 2.
     */
    private int determineDeviceShiftType(List<int[]> pairs) {
        if (pairs == null || pairs.isEmpty())
            return 1;

        // shift start = the start right after the biggest empty gap between sorted starts
        List<Integer> starts = new ArrayList<>();
        for (int[] p : pairs)
            starts.add(p[0]);
        starts.sort(null);
        int n = starts.size();
        int shiftStart = starts.get(0);
        int biggestGap = -1;
        for (int i = 0; i < n; i++) {
            int cur = starts.get(i);
            int next = starts.get((i + 1) % n);
            int gap = (i + 1 < n) ? (next - cur) : ((86400 - cur) + next); // wrap last→first
            if (gap > biggestGap) {
                biggestGap = gap;
                shiftStart = next;
            }
        }

        // roll each trip forward from shiftStart and track the latest end
        int latestEnd = Integer.MIN_VALUE;
        for (int[] p : pairs) {
            int s = p[0], e = p[1];
            int rolledStart = (s >= shiftStart) ? s : s + 86400; // before start ⇒ next day
            int rolledEnd = (e > s) ? rolledStart + (e - s) : rolledStart + (86400 - s + e); // wrap
            latestEnd = Math.max(latestEnd, rolledEnd);
        }

        int result = (latestEnd >= 86400) ? 2 : 1; // crosses midnight ⇒ cross-day shift
        log.info("determineDeviceShiftType pairs={} shiftStart={}s latestEnd={}s -> shiftType={}", pairs.size(),
                shiftStart, latestEnd, result);
        return result;
    }

    private static int determineShiftType(LocalDate scheduleDate, List<String> beat) {
        LocalDateTime scheduleStart = null;
        LocalDateTime scheduleEnd = null;
        LocalDateTime lastEnd = null;

        for (int i = 6, trip = 1; i < beat.size(); i += 2, trip++) {
            String startStr = beat.get(i);
            String endStr = beat.get(i + 1);

            if (isNaOrBlank(startStr) || isNaOrBlank(endStr))
                continue;

            String[] startParts = startStr.split(":");
            String[] endParts = endStr.split(":");

            LocalDateTime startDT = scheduleDate.atTime(Integer.parseInt(startParts[0]),
                    Integer.parseInt(startParts[1]));
            LocalDateTime endDT = scheduleDate.atTime(Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]));

            // adjust rolling forward
            if (lastEnd != null && !startDT.isAfter(lastEnd)) {
                startDT = startDT.plusDays(1);
                endDT = endDT.plusDays(1);
            } else if (!endDT.isAfter(startDT)) {
                endDT = endDT.plusDays(1);
            }

            lastEnd = endDT;

            if (scheduleStart == null || startDT.isBefore(scheduleStart))
                scheduleStart = startDT;
            if (scheduleEnd == null || endDT.isAfter(scheduleEnd))
                scheduleEnd = endDT;

            // 🔹 Debug log for each trip
            System.out.println("Trip " + trip + ": start=" + startDT + ", end=" + endDT);
        }

        if (scheduleStart == null) {
            System.out.println("⚠️ No valid trips found. Returning 1");
            return 1; // no valid trips
        }

        System.out.println("Final Schedule Start=" + scheduleStart + ", End=" + scheduleEnd);

        // Shift type = 2 if start and end fall on different days
        int result = scheduleEnd.toLocalDate().isAfter(scheduleStart.toLocalDate()) ? 2 : 1;
        System.out.println("ShiftType=" + result);
        return result;
    }

    public int getTripShiftTypeId(String startTime, String endTime) {
        log.info("getTripShiftTypeId: startTime:" + startTime + " endTime:" + endTime);
        LocalTime start = LocalTime.parse(stringToLocalTime(startTime).toString()); // e.g., "22:00"
        LocalTime end = LocalTime.parse(stringToLocalTime(endTime).toString()); // e.g., "08:00"

        // If end time is before start time, it means the shift crosses midnight
        if (end.isBefore(start)) {
            return 2; // Shift spans across two days
        } else {
            return 1; // Shift within same day
        }
    }

    private void sendPeriodCommandToDevice(Map<Long, List<BeatEntity>> groupedRecords, String divisionId) {
        log.info("sendPeriodCommandToDevice call");

        groupedRecords.forEach((deviceImei, records) -> {
            log.info("sendPeriodCommandToDevice " + deviceImei + "   " + records);

            if (records == null || records.isEmpty())
                return;

            // Per-device command list — must be local so each device only receives its own PERIOD commands.
            // A shared list accumulates across devices and re-sends earlier devices' commands every iteration.
            List<DeviceCommandHistoryEntity> command = new ArrayList<>();

            int buffer = 3600;

            // #6 — bound the window across ALL trips, not just first/last (multi-row uploads can interleave trips).
            // Take min/max on the SIGNED trip times: night-shift trips are stored with evening times shifted
            // negative (see getTime), which keeps the timeline monotonic around midnight. We must aggregate on the
            // signed values and convert to clock seconds AFTERWARDS — converting first (handleNegativeTime before
            // min/max) folds everything into 0..86399 and collapses a multi-trip overnight span into the wrong,
            // inverted window (e.g. a 20:00→04:00 device would yield the idle midday window instead).
            int signedStart = records.stream().mapToInt(r -> Math.toIntExact(r.getStartTime())).min().orElse(0);
            int signedEnd = records.stream().mapToInt(r -> Math.toIntExact(r.getEndTime())).max().orElse(0);
            int start = handleNegativeTime(signedStart);
            int end = handleNegativeTime(signedEnd);
            BeatEntity firstTrip = records.get(0);

            int adjustedStart = Math.max(60, start - buffer);
            int adjustedEnd = Math.min(86399, end + buffer + 60);

            boolean isCrossDay = adjustedEnd < adjustedStart;
            log.info("adjustedStart: " + adjustedStart + " adjustedEnd: " + adjustedEnd);
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
                    log.info("1 Command to be sent: " + commandEntity.getCommand());
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
                    log.info("2 Command to be sent: " + commandEntity.getCommand());
                    command.add(commandEntity);
                }
            }

            deviceCommandServiceImpl.sendCommand(command);

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private int handleNegativeTime(int time) {
        return time < 0 ? time + 86400 : time;
    }

    private String convertSecondsToHHmm(int seconds) {
        seconds = seconds % 86400;
        int hrs = seconds / 3600;
        int mins = (seconds % 3600) / 60;
        return String.format("%02d:%02d", hrs, mins);
    }

    // private void sendPeriodCommandToDevice(Map<Long, List<BeatEntity>> groupedRecords, String divisionId) {
    // List<DeviceCommandHistoryEntity> command = new ArrayList<>();
    //
    // // Step 4 Send command to new device
    // groupedRecords.forEach((deviceImei, records) -> {
    //
    // // Convert startTime and endTime to HH:mm and collect into a List
    // List<String[]> timeList = records.stream()
    // .map(info -> new String[] {
    // convertSecondsToHHmm(handleNegativeTime(Math.toIntExact(info.getStartTime()))),
    // convertSecondsToHHmm(handleNegativeTime(Math.toIntExact(info.getEndTime()))),
    // isCrossDayInterval(Math.toIntExact(info.getStartTime()), Math.toIntExact(info.getEndTime()))
    // ? "Crosses Days" : "Single Day"
    //
    // }) // Convert startTime and endTime to HH:mm format
    // .collect(Collectors.toList());
    //
    // // Print the formatted string
    // // System.out.println("All times:");
    // // System.out.println(timeListString);
    //
    // // Split the timeList into "last day" and "current day"
    // // Separate into current day and next day
    // List<String[]> currentDayTimes = new ArrayList<>();
    // List<String[]> nextDayTimes = new ArrayList<>();
    //
    // timeList.forEach(arr -> {
    // String startTime = arr[0];
    // String endTime = arr[1];
    // String intervalType = arr[2];
    //
    // if (isCurrentDayTime(startTime, endTime)) {
    // currentDayTimes.add(arr);
    // } else {
    // nextDayTimes.add(arr);
    // }
    // });
    //
    // // Find min startTime from currentDayTimes
    // String minCurrentDayTime = currentDayTimes.stream().map(arr -> arr[0]) // Get end times
    // .max(Comparator.naturalOrder()) // Find the maximum end time
    // .orElseGet(() -> nextDayTimes.stream().map(arr -> arr[0]) // Use max from currentDayTimes if
    // // nextDayTimes is empty
    // .min(Comparator.naturalOrder()) // Find the maximum end time from currentDayTimes
    // .orElse("N/A"));
    //
    // // Find max endTime from nextDayTimes, or use max from currentDayTimes if nextDayTimes is empty
    // String maxNextDayTime = nextDayTimes.stream().map(arr -> arr[1]) // Get end times
    // .max(Comparator.naturalOrder()) // Find the maximum end time
    // .orElseGet(() -> currentDayTimes.stream().map(arr -> arr[1]) // Use max from currentDayTimes if
    // .max(Comparator.naturalOrder()) // Find the maximum end time from currentDayTimes
    // .orElse("N/A"));
    //
    // // System.out.println("Current Day Times:");
    // currentDayTimes.forEach(
    // arr -> System.out.println("StartTime: " + arr[0] + ", EndTime: " + arr[1] + ", Type: " + arr[2]));
    //
    // // System.out.println("\nNext Day Times:");
    // nextDayTimes.forEach(
    // arr -> System.out.println("StartTime: " + arr[0] + ", EndTime: " + arr[1] + ", Type: " + arr[2]));
    //
    // // Print the min and max times
    // System.out.println("\nMin Start Time (Current Day): " + minCurrentDayTime);
    // System.out.println("Max End Time (Next Day): " + maxNextDayTime);
    // // System.out.println("convertTimeToSeconds(minCurrentDayTime): " +
    // // convertTimeToSeconds(minCurrentDayTime));
    //
    // if (currentDayTimes.isEmpty() && convertTimeToSeconds(minCurrentDayTime) > 2100) {
    // // PERIOD,1,1,0,04:31-18:29,00:00-00:00#
    // DeviceCommandHistoryEntity commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    //
    // commandEntity.setCommand(
    // "PERIOD,1,1,0," + convertSecondsToHHmm(convertTimeToSeconds(minCurrentDayTime) - 1740) + "-"
    // + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740) + ",00:00-00:00");
    //
    // command.add(commandEntity);
    //
    // commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    // commandEntity.setCommand(
    // "PERIOD,1,1,1," + convertSecondsToHHmm(convertTimeToSeconds(minCurrentDayTime) - 1740) + "-"
    // + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740) + ",00:00-00:00");
    // command.add(commandEntity);
    //
    // commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    // commandEntity.setCommand(
    // "PERIOD,1,1,2," + convertSecondsToHHmm(convertTimeToSeconds(minCurrentDayTime) - 1740) + "-"
    // + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740) + ",00:00-00:00");
    // command.add(commandEntity);
    //
    // // log.info("command list size1---" + command);
    // deviceCommandServiceImpl.sendCommand(command);
    //
    // } else if (currentDayTimes.isEmpty() && convertTimeToSeconds(minCurrentDayTime) <= 2100) {
    // DeviceCommandHistoryEntity commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    //
    // commandEntity.setCommand("PERIOD,1,1,0,23:30" + "-23:59,00:01-"
    // + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740));
    // command.add(commandEntity);
    //
    // commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    // commandEntity.setCommand("PERIOD,1,1,1,23:30" + "-23:59,00:01-"
    // + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740));
    // command.add(commandEntity);
    //
    // commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    // commandEntity.setCommand("PERIOD,1,1,2,23:30" + "-23:59,00:01-"
    // + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740));
    // command.add(commandEntity);
    //
    // // log.info("command list size3---" + command);
    // deviceCommandServiceImpl.sendCommand(command);
    //
    // } else {
    // DeviceCommandHistoryEntity commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    //
    // commandEntity.setCommand(
    // "PERIOD,1,1,0," + convertSecondsToHHmm(convertTimeToSeconds(minCurrentDayTime) - 1740)
    // + "-23:59,00:01-" + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740));
    // command.add(commandEntity);
    //
    // commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    // commandEntity.setCommand(
    // "PERIOD,1,1,1," + convertSecondsToHHmm(convertTimeToSeconds(minCurrentDayTime) - 1740)
    // + "-23:59,00:01-" + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740));
    // command.add(commandEntity);
    //
    // commandEntity = new DeviceCommandHistoryEntity();
    // commandEntity.setDeviceName(records.get(0).getDevice_name());
    // commandEntity.setDeviceImei(deviceImei);
    // commandEntity.setLoginName("PrimeTrack");
    // commandEntity.setDivisionId(divisionId);
    // commandEntity.setCommand(
    // "PERIOD,1,1,2," + convertSecondsToHHmm(convertTimeToSeconds(minCurrentDayTime) - 1740)
    // + "-23:59,00:01-" + convertSecondsToHHmm(convertTimeToSeconds(maxNextDayTime) + 1740));
    // command.add(commandEntity);
    //
    // // log.info("command list size3---" + command);
    // deviceCommandServiceImpl.sendCommand(command);
    //
    // }
    // try {
    // Thread.sleep(5);
    // } catch (InterruptedException e) {
    // throw new RuntimeException(e);
    // }
    // });
    //
    // }

    @Override
    public List<DeviceBeatDto> getDeviceTypeBeat(String divisionId, Integer deviceType) {
        log.info("divisionId: {}, deviceType: {}", divisionId, deviceType);

        List<DeviceEntity> existingDevices = deviceRepository.findByDivisionIdAndDeviceTypeId(divisionId, deviceType);

        if (existingDevices == null || existingDevices.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString());
        }

        List<Long> imeis = existingDevices.stream().map(DeviceEntity::getDeviceImei).filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<BeatEntity> beatEntities = beatRepository.findByDeviceImeiInAndActiveStatus(imeis, true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_FOUND.toString()));

        return beatEntities.stream().map(beat -> {
            return DeviceBeatDto.builder().beatId(beat.getId()).tEndKm(beat.getTripEndKm())
                    .sectionName(beat.getSectionName()).activeStatus(beat.getActiveStatus())
                    .studentId(beat.getStudentId()).bEndTime(beat.getBreakEndTime())
                    .bStartTime(beat.getBreakStartTime()).startTime(beat.getStartTime())
                    .deviceName(beat.getDevice_name()).deviceNo(beat.getDeviceNo()).deviceTypeId(beat.getDeviceTypeId())
                    .uploadedBy(divisionLoginService.getDivisionFromId(beat.getCreatedBy()))
                    .approvedBy(divisionLoginService.getDivisionFromId(beat.getApprovedBy()))
                    .approvedStatus(beat.getApprovedStatus()).tripNo(beat.getTripNo()).endTime(beat.getEndTime())
                    .deviceImei(beat.getDeviceImei()).shiftType(beat.getShiftType()).sectionName(beat.getSectionName())
                    .tStartKm(beat.getTripStartKm()).build();
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<BeatEntity> updateBeat(DeviceBeatRequest beat) {
        Optional<BeatEntity> optionalBeatEntityOld = beatRepository.findById(new ObjectId(beat.getBeatId()));

        if (optionalBeatEntityOld.isPresent()) {
            BeatEntity beatEntityOld = optionalBeatEntityOld.get();
            updateActiveBeatsToFalse(beat.getBeatId(), beat.getUpdatedBy());

            BeatEntity beatEntity = new BeatEntity();
            // Copy data from the old entity and the incoming request

            beatEntity.setDeviceImei(beatEntityOld.getDeviceImei());
            beatEntity.setSectionName(beat.getSectionName());
            beatEntity.setDeviceTypeId(beatEntityOld.getDeviceTypeId());
            beatEntity.setDevice_name(DeviceNameUtil.format(beat.getDeviceName(), beat.getDeviceNo()));
            beatEntity.setShiftType(beat.getShiftType());
            beatEntity.setTShiftType(Objects.requireNonNullElse(beatEntity.getTShiftType(), 1));
            beatEntity.setTripNo(beat.getTripNo());
            beatEntity.setDeviceNo(beatEntityOld.getDeviceNo());
            beatEntity.setActiveStatus(beat.getActiveStatus()); // Activating new beat
            beatEntity.setApprovedStatus(beat.getActiveStatus()); // Mark as approved by default

            // Convert break start and end times to seconds
            beatEntity.setBreakStartTime(getSecFromTime(beat.getBstartTime()));
            beatEntity.setBreakEndTime(getSecFromTime(beat.getBendTime()));

            // Set start and end kilometers
            beatEntity.setTripStartKm(beat.getTstartKm());
            beatEntity.setTripEndKm(beat.getTendKm());

            // Convert start and end times into epoch seconds
            long[] times = getTime(beat.getStartTime(), beat.getEndTime(), beat.getShiftType());
            beatEntity.setStartTime(times[0]);
            beatEntity.setEndTime(times[1]);

            // Set audit fields
            beatEntity.setCreatedAt(System.currentTimeMillis() / 1000);
            beatEntity.setCreatedBy(beat.getUpdatedBy());
            beatEntity.setUpdatedBy(beat.getUpdatedBy());
            beatEntity.setUpdatedAt(System.currentTimeMillis() / 1000);
            beatEntity.setTripCount(beatEntityOld.getTripCount());

            // Save the new beat entity
            BeatEntity savedEntity = beatRepository.save(beatEntity);

            // Return the saved entity wrapped in an Optional
            return Optional.of(savedEntity);
        } else {
            // Return an empty Optional if the old entity isn't found
            return Optional.empty();
        }
    }

    @Override
    public Optional<BeatEntity> deleteBeat(String beatId, String updatedBy) {
        Optional<BeatEntity> optionalBeatEntityOld = beatRepository.findById(new ObjectId(beatId));

        if (optionalBeatEntityOld.isPresent()) {
            BeatEntity beatEntityOld = optionalBeatEntityOld.get();
            beatEntityOld.setActiveStatus(false);
            beatEntityOld.setUpdatedBy(updatedBy);
            beatEntityOld.setUpdatedAt(System.currentTimeMillis() / 1000);
            BeatEntity savedEntity = beatRepository.save(beatEntityOld);

            // Return the saved entity wrapped in an Optional
            return Optional.of(savedEntity);
        } else {
            // Return an empty Optional if the old entity isn't found
            return Optional.empty();
        }
    }

    @Override
    public List<BeatEntity> approveMultipleBeats(String beatIdsCsv, String updatedBy) {
        // 1. Parse comma-separated IDs into ObjectId list
        List<ObjectId> ids = Arrays.stream(beatIdsCsv.split(",")).map(String::trim).filter(id -> !id.isEmpty())
                .map(ObjectId::new).toList();

        // 2. Fetch all matching beats
        List<BeatEntity> beats = beatRepository.findAllById(ids);

        // 3. Update each beat entity
        beats.forEach(beat -> {
            beat.setApprovedStatus(true);
            beat.setApprovedBy(updatedBy);
            beat.setUpdatedBy(updatedBy);
            beat.setUpdatedAt(System.currentTimeMillis() / 1000);
        });

        // 4. Save all updated beats
        return beatRepository.saveAll(beats);
    }

    public List<BeatGroupByFileDTO> getUnapprovedGroupedByRefFile() {
        List<BeatGroupByFileDTO> result = beatRepository.findUnapprovedGroupedByRefFileName();

        result.forEach(group -> {
            // Set createdAt
            group.getDevices().stream().flatMap(device -> device.getBeats().stream()).map(BeatEntity::getCreatedAt)
                    .filter(Objects::nonNull).findFirst().ifPresent(group::setCreatedAt);

            // Get first non-null device IMEI
            group.getDevices().stream().map(DeviceGroupDTO::getDeviceImei).filter(Objects::nonNull).findFirst()
                    .ifPresent(imei -> {
                        DeviceEntity device = deviceService.getDevicesDetails(imei);
                        if (device != null && device.getDivisionId() != null) {
                            String divisionId = device.getDivisionId();
                            group.setDivision_id(divisionId);

                            // Safe division name fetch
                            String division = divisionLoginService.getDivisionFromId(divisionId);
                            String divisionName = (division != null) ? division : divisionId;

                            group.setDivision_name(divisionName);
                        }
                    });

        });

        return result;
    }

    private int getShiftTypeId(String startTime, String endTime, Integer deviceTypeId) {
        int shiftType = 1;

        // if (deviceTypeId != 6) {
        String[] startTimeArray = startTime.split(COLON);
        String[] endTimeArray = endTime.split(COLON);
        log.info("getShiftTypeId________" + startTime + "____________" + endTime);

        long startTimeStamp = 0 + (60 * Integer.parseInt(startTimeArray[1]))
                + (3600 * Integer.parseInt(startTimeArray[0]));
        long endTimeStamp = 0 + (60 * Integer.parseInt(endTimeArray[1])) + (3600 * Integer.parseInt(endTimeArray[0]));
        log.info("getShiftTypeId________" + startTimeStamp + "____________" + endTimeStamp);

        // log.info("1________"+startTimeStampMapping+"____________"+endTimeStampMapping );
        LocalTime startTimeLocal = stringToLocalTime(startTime);
        LocalTime endTimeLocal = stringToLocalTime(endTime);
        log.info("local________" + startTimeLocal + "____________" + endTimeLocal);

        if (!startTimeLocal.isBefore(endTimeLocal) || (startTimeStamp >= 43200)) {
            shiftType = 2;

        } else if (startTimeStamp >= 0 && startTimeStamp < 32400 && endTimeStamp < 32400) {
            shiftType = 2;
        }
        // }

        // log.info("2________"+startTimeStampMapping+"____________"+endTimeStampMapping );
        return shiftType;
    }

    public LocalTime stringToLocalTime(String timeStr) {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedTimeStr = formatTimeString(timeStr);
            return LocalTime.parse(formattedTimeStr, timeFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid time format: " + timeStr);
            return null;
        }
    }
    // public static LocalTime stringToLocalTime(String timeStr) {
    // DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    // return LocalTime.parse(formattedTimeStr, timeFormatter);
    // }

    public static String formatTimeString(String timeStr) {
        Pattern pattern = Pattern.compile("(\\d{1,2}):(\\d{1,2})");
        Matcher matcher = pattern.matcher(timeStr);
        if (matcher.matches()) {
            String hour = matcher.group(1);
            String minute = matcher.group(2);
            return String.format("%02d:%02d", Integer.parseInt(hour), Integer.parseInt(minute));
        }
        return timeStr;
    }

    private long getDeviceImei(String divisionId, int deviceNo) {
        DeviceEntity deviceImei = deviceRepository.findByNoTrackDivId((divisionId), deviceNo);
        if (deviceImei != null)
            return deviceImei.getDeviceImei();
        return 0;
    }

    long[] getTime(String startTime, String endTime, int shiftType) {
        String[] startTimeArray = startTime.split(COLON);
        String[] endTimeArray = endTime.split(COLON);
        // log.info("________"+startTime+"____________"+endTime );

        long startTimeStamp = 0 + (60 * Integer.parseInt(startTimeArray[1]))
                + (3600 * Integer.parseInt(startTimeArray[0]));
        long endTimeStamp = 0 + (60 * Integer.parseInt(endTimeArray[1])) + (3600 * Integer.parseInt(endTimeArray[0]));

        long startTimeStampMapping = startTimeStamp;
        long endTimeStampMapping = endTimeStamp;
        // log.info("1________"+startTimeStampMapping+"____________"+endTimeStampMapping );

        if (shiftType == 2) {
            if (startTimeStamp > 43200)
                startTimeStampMapping = startTimeStamp - 86400;
            if (endTimeStamp > 43200)
                endTimeStampMapping = endTimeStamp - 86400;
        }

        log.info("start time {} {} {}", startTimeStampMapping, endTimeStampMapping, shiftType);
        // log.info("2________"+startTimeStampMapping+"____________"+endTimeStampMapping );
        return new long[] { startTimeStampMapping, endTimeStampMapping };
    }

    // N/A in any case/variant (N/A, NA, na, Na, n/a, …) or blank counts as "no trip".
    private static boolean isNaOrBlank(String s) {
        return s == null || s.isBlank() || s.equalsIgnoreCase("N/A") || s.equalsIgnoreCase("NA");
    }

    private String validateBeatFile(List<List<String>> beats) {
        String regex = "^([01]?[0-9]|2[0-3]):([0-5]?[0-9])$";
        StringBuilder errorInSheet = new StringBuilder();
        for (int i = 9; i < beats.size(); i++) {
            List<String> beat = beats.get(i);
            if (beat.size() % 2 == 0) {
                errorInSheet.append("\nRow:").append(i + 1).append(" ").append(" : Please enter End time of ");
            }
            for (int j = 7; j < beat.size(); j++) {
                if (!StringUtils.isBlank(beat.get(j)) && !Pattern.matches(regex, beat.get(j))) {
                    log.info(beat.get(j) + "   Time in Cell is invalid please enter valid data.");

                    errorInSheet.append("\nRow:").append(i + 1).append(" Col:").append(j + 1)
                            .append(" Time in Cell is invalid please enter valid data.");
                }
            }
        }
        return errorInSheet.toString();
    }

    /**
     * Resolve a device-type id from either form the clients send: the manual-entry UI posts the numeric id ("2"), the
     * Excel upload posts the type name ("Patrolman"). Numeric strings are used as-is; otherwise fall back to name
     * lookup.
     */
    private int resolveDeviceTypeId(String input) {
        if (input == null || input.isBlank())
            return 0;
        String s = input.trim();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return deviceTypeService.getDeviceTypeId(s);
        }
    }

    /**
     * Removes any trailing "-<deviceNo>" the client may have round-tripped back into the name, so the suffix is not
     * compounded on every re-submit (e.g. "Rupesh-1-1-1" + deviceNo 1 → "Rupesh"). Returns "" for null/blank input.
     */
    private String stripDeviceNoSuffix(String name, int deviceNo) {
        if (name == null)
            return "";
        String s = name.trim();
        String suffix = "-" + deviceNo;
        while (s.endsWith(suffix))
            s = s.substring(0, s.length() - suffix.length()).trim();
        return s;
    }

    long getSecFromTime(String startTime) {
        String[] startTimeArray = startTime.split(COLON);
        long startTimeStamp = 0 + (60 * Integer.parseInt(startTimeArray[1]))
                + (3600 * Integer.parseInt(startTimeArray[0]));
        return startTimeStamp;

    }
}