package com.primesys.adminserviceserver.modules.jobs.schedulers;

import com.primesys.adminservicecommon.dto.DeviceTrip;
import com.primesys.adminservicecommon.dto.report.DivisionLoginScheduleReportDTO;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.TripEntity;
import com.primesys.adminservicemongodb.model.DeviceHistory;
import com.primesys.adminservicemongodb.model.DeviceImeiNoOnly;
import com.primesys.adminservicemongodb.model.JobOrderDevice;
import com.primesys.adminservicemongodb.repository.*;
import com.primesys.adminserviceserver.modules.jobs.services.JobOrderExecutionLogService;
import com.primesys.adminserviceserver.modules.reports.dtos.TripStatusReportSummaryCreateDTO;
import com.primesys.adminserviceserver.modules.reports.utils.ReportDistanceUtility;
import com.primesys.adminserviceserver.service.DeviceService;
import com.primesys.adminserviceserver.utility.DateTimeUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportScheduledJob {

    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;
    private final DivisionLoginRepository divisionLoginRepository;
    private final TripDataRepository tripDataRepository;

    /// Services call
    private final JobOrderExecutionLogService jobOrderExecutionLogService;
    private final DeviceHistoryRepository deviceHistoryRepository;

    private final ExecutorService reportExecutor;

    // @Scheduled(cron = "0 16 13 * * *", zone = "Asia/Kolkata")
    // public void runTripSummaryJob() {
    // System.out.println("Job triggered at 13:13");
    // executeScheduleTripReportSummaryKeymanJob();
    // }

    ///
    /// trip report summary for keyman devices start from here with steps
    ///

    public List<TripStatusReportSummaryCreateDTO> executeScheduleTripReportSummaryKeymanJob() {
        List<String> divisionIds = List.of("68b9290446c12a561f41c735", "66f790bf19fe834a3b07023c");
        List<DivisionLoginScheduleReportDTO> divisionLoginScheduleReportDTOS = getDevicesWithOfDeviceType(
                "68b9290446c12a561f41c735", 8);
        List<TripStatusReportSummaryCreateDTO> tripStatusReportSummaryCreateDTOS = processKeymanScheduledReport(
                divisionLoginScheduleReportDTOS, 8);
        return tripStatusReportSummaryCreateDTOS;

        // for (int i=0; i<divisionIds.size() -1; i++) {
        //
        //
        // }
    }

    ///
    /// Step 1:
    /// desc: this will fetch division and subdivisions of the given division id with path and device_list
    /// then filter it with the give device type id and return List of
    /// {
    /// path:"",
    /// trackDivisionId:"",
    /// deviceImeis:[
    /// {
    /// deviceImei:0,
    /// deviceNo: 0
    /// }
    /// ]
    /// }
    ///

    public List<DivisionLoginScheduleReportDTO> getDevicesWithOfDeviceType(String divisionId, int deviceTypeId) {
        List<DivisionLoginEntity> divisionLoginList = divisionLoginRepository.findDivisionUsersWithDevices(divisionId);
        List<DivisionLoginScheduleReportDTO> scheduleReportDTOS = new ArrayList<DivisionLoginScheduleReportDTO>();

        List<Integer> deviceNos = divisionLoginList.stream().map(DivisionLoginEntity::getDeviceList)
                .filter(Objects::nonNull).flatMap(s -> Arrays.stream(s.split(","))).map(String::trim)
                .filter(s -> !s.isEmpty()).map(Integer::parseInt).distinct().toList();
        if (deviceNos.isEmpty()) {
            return Collections.emptyList();
        }

        List<DeviceImeiNoOnly> imeiDevices = deviceRepository.findDeviceNosByDivisionAndDeviceType(divisionId,
                deviceTypeId, deviceNos);

        int allcount = 0;

        for (DivisionLoginEntity divisionLoginEntity : divisionLoginList) {
            String divisionDeviceList = divisionLoginEntity.getDeviceList();
            List<Integer> devicesList = Arrays.stream(divisionDeviceList.split(",")).filter(s -> !s.isBlank())
                    .map(Integer::parseInt).toList();
            List<DeviceImeiNoOnly> validDevices = new ArrayList<DeviceImeiNoOnly>();
            for (Integer integer : devicesList) {
                for (DeviceImeiNoOnly device : imeiDevices) {
                    if (integer.equals(device.getDeviceNo())) {
                        validDevices.add(device);
                        break;
                    }
                }
            }
            allcount = allcount + validDevices.size();

            DivisionLoginScheduleReportDTO reportDTO = new DivisionLoginScheduleReportDTO();
            reportDTO.setPath(divisionLoginEntity.getPath());
            reportDTO.setTrackDivisionId(divisionId);
            reportDTO.setDeviceNoImeis(validDevices);
            reportDTO.setName(divisionLoginEntity.getName());
            scheduleReportDTOS.add(reportDTO);
        }

        return scheduleReportDTOS;
    }

    ///
    /// Step 2: Process Keyman Report start
    /// This is the step where each trip report will be created
    ///

    private List<TripStatusReportSummaryCreateDTO> processKeymanScheduledReport(
            List<DivisionLoginScheduleReportDTO> divisionLoginScheduleReportDTOS, int deviceTypeId) {
        StringBuilder totalRemark = new StringBuilder();
        List<JobOrderDevice> failedDevices = new ArrayList<JobOrderDevice>();
        List<TripStatusReportSummaryCreateDTO> tripStatusReportSummaryCreateDTOS = new ArrayList<TripStatusReportSummaryCreateDTO>();
        for (DivisionLoginScheduleReportDTO divisionLoginScheduleReportDTO : divisionLoginScheduleReportDTOS) {
            TripStatusReportSummaryCreateDTO remark = processReportPerDivision(divisionLoginScheduleReportDTO,
                    divisionLoginScheduleReportDTO.getTrackDivisionId(), failedDevices, deviceTypeId);
            tripStatusReportSummaryCreateDTOS.add(remark);
        }
        return tripStatusReportSummaryCreateDTOS;
    }

    ///
    /// Step 2.1: process report per division / subdivision
    /// this function will loop every subdivision means one division at a time
    /// {
    /// path:"",
    /// trackDivisionId:"",
    /// deviceimeis:[]
    /// }
    /// Get Every device trip detail to process

    private TripStatusReportSummaryCreateDTO processReportPerDivision(
            DivisionLoginScheduleReportDTO divisionLoginScheduleReportDTO, String divisionId,
            List<JobOrderDevice> failedDevices, int deviceTypeId) {
        TripStatusReportSummaryCreateDTO tripStatusReportSummaryCreateDTO = new TripStatusReportSummaryCreateDTO();
        List<String> deviceOffs = new ArrayList<String>();
        List<String> offTracks = new ArrayList<String>();
        List<String> tripCompletes = new ArrayList<String>();
        List<String> tripNotCompletes = new ArrayList<String>();
        List<String> overspeeds = new ArrayList<String>();
        List<String> delayedStarts = new ArrayList<String>();

        for (DeviceImeiNoOnly deviceImei : divisionLoginScheduleReportDTO.getDeviceNoImeis()) {
            DeviceTrip trip = extractTripTime(deviceImei.getDeviceImei());

            log.info("Device IMei: {}", deviceImei.getDeviceImei());

            if (trip.getStartTimeStamp() == -1L) {
                JobOrderDevice failedDevice = new JobOrderDevice();
                failedDevice.setDeviceImei(deviceImei.getDeviceImei());
                failedDevice.setTrackDivisionId(divisionId);
                failedDevice
                        .setRemark("Trip was not scheduled for this device so this device was not processed further");
                failedDevices.add(failedDevice);
                continue;
            } else {

                List<DeviceHistory> histories = getDeviceHistory(deviceImei.getDeviceImei(), trip.getStartTimeStamp(),
                        trip.getEndTimeStamp());

                if (histories.isEmpty()) {
                    deviceOffs.add(deviceImei.getDeviceNo().toString());
                } else {
                    List<DeviceHistory> valid = histories.stream()
                            .filter(ReportDistanceUtility::distanceDiffLessThan50M).toList();
                    if (valid.isEmpty()) {
                        offTracks.add(deviceImei.getDeviceNo().toString());
                    } else if (valid.size() < 6) {
                        tripNotCompletes.add(deviceImei.getDeviceNo().toString());
                    } else {
                        if (checkDelayStart(histories.get(0), trip.getStartTimeStamp())) {
                            delayedStarts.add(deviceImei.getDeviceNo().toString());
                        }

                        if (checkOverspeed(histories)) {
                            overspeeds.add(deviceImei.getDeviceNo().toString());
                        }

                        if (tripExistsAtoB(valid, trip.getTripStartKm(), trip.getTripEndKm())
                                || tripExistsBtoA(valid, trip.getTripStartKm(), trip.getTripEndKm())) {
                            tripCompletes.add(deviceImei.getDeviceNo().toString());
                        } else {
                            tripNotCompletes.add(deviceImei.getDeviceNo().toString());
                        }

                    }
                }
            }
        }
        String deviceOff = deviceOffs.isEmpty() ? "" : String.join(", ", deviceOffs);
        String delayStart = delayedStarts.isEmpty() ? "" : String.join(", ", delayedStarts);
        String tripComplete = tripCompletes.isEmpty() ? "" : String.join(", ", tripCompletes);
        String offTrack = offTracks.isEmpty() ? "" : String.join(", ", offTracks);
        String overspeed = overspeeds.isEmpty() ? "" : String.join(", ", overspeeds);
        String tripNotComplete = tripNotCompletes.isEmpty() ? "" : String.join(", ", tripNotCompletes);

        tripStatusReportSummaryCreateDTO.setDeviceOff(deviceOff);
        tripStatusReportSummaryCreateDTO.setDelayedStart(delayStart);
        tripStatusReportSummaryCreateDTO.setTripCompleted(tripComplete);
        tripStatusReportSummaryCreateDTO.setOffTrack(offTrack);
        tripStatusReportSummaryCreateDTO.setOverSpeed(overspeed);
        tripStatusReportSummaryCreateDTO.setTripNotCompleted(tripNotComplete);

        tripStatusReportSummaryCreateDTO.setReportOfTheDay(DateTimeUtility.toMidNightEpoch());
        tripStatusReportSummaryCreateDTO.setPath(divisionLoginScheduleReportDTO.getPath());

        tripStatusReportSummaryCreateDTO.setName(divisionLoginScheduleReportDTO.getName());
        tripStatusReportSummaryCreateDTO
                .setShiftType(divisionLoginScheduleReportDTO.getDeviceNoImeis().get(0).getShiftType());

        tripStatusReportSummaryCreateDTO.setTrackDivisionId(divisionLoginScheduleReportDTO.getTrackDivisionId());

        tripStatusReportSummaryCreateDTO.setDeviceTypeId(deviceTypeId);
        return tripStatusReportSummaryCreateDTO;
    }

    private boolean checkDelayStart(DeviceHistory history, Long startTime) {
        long deviceTime = history.getTimestamp();
        long tripActualStartTime = DateTimeUtility.toMidNightEpoch() + startTime;
        long diff = tripActualStartTime - deviceTime;
        return diff > 1800;
    }

    private boolean checkDelayEnd(DeviceHistory history, Long endTime) {
        long deviceTime = history.getTimestamp();
        long tripActualEndTime = DateTimeUtility.toMidNightEpoch() + endTime;
        long diff = tripActualEndTime - deviceTime;
        return diff > 1800;
    }

    /// step 2.2: Trips
    /// get the device trip start and end time
    private DeviceTrip extractTripTime(long deviceImei) {
        List<TripEntity> entities = tripDataRepository.findAllApprovedActiveTrips(deviceImei).stream()
                .sorted(Comparator.comparing(TripEntity::getTripNo)).toList();

        if (entities.isEmpty()) {
            return new DeviceTrip(-1L, 0L, 0, 0d, 0d, "", 0L);
        }

        long startTimeStamp = entities.get(0).getStartTime();
        long endTimeStamp = entities.get(entities.size() - 1).getEndTime();
        int tripCount = tripCount(entities);
        double startKm = entities.get(0).getTripStartKm();
        double endKm = entities.get(entities.size() - 1).getTripEndKm();
        String sectionName = entities.get(0).getSectionName();
        return new DeviceTrip(startTimeStamp, endTimeStamp, tripCount, startKm, endKm, sectionName,
                entities.get(0).getDeviceImei());
    }

    private int tripCount(List<TripEntity> entities) {
        return entities.size();
    }

    /// step 2.3: History
    /// get Device all history
    public List<DeviceHistory> getDeviceHistory(Long deviceImei, Long start, Long end) {
        Long startTime = DateTimeUtility.toMidNightEpoch() + start;
        Long endTime = DateTimeUtility.toMidNightEpoch() + end;

        return deviceHistoryRepository.findLightHistoryForTrip(deviceImei, startTime, endTime,
                Sort.by(Sort.Direction.ASC, "timestamp"));
    }

    public boolean tripExistsAtoB(List<DeviceHistory> validHistories, Double startKm, Double endKm) {
        double startKM = startKm - 0.01;
        double endKM = endKm + 0.01;
        double endKMLess2M = endKM - 0.02;

        int currentDir = 0;
        int streak = 0;

        List<DeviceHistory> current = new ArrayList<>();

        for (int i = 1; i < validHistories.size(); i++) {

            DeviceHistory prev = validHistories.get(i - 1);
            DeviceHistory curr = validHistories.get(i);

            int dir = direction(prev, curr);

            if (currentDir == 0) {
                currentDir = dir;
            }

            if (dir == currentDir || dir == 0) {
                streak = 0;
                current.add(curr);
                continue;
            }

            streak++;

            if (streak >= 5) {
                int tripLastIndex = current.size() - 5;

                double lastIndexedValKM = ReportDistanceUtility
                        .kmDistValue(current.get(tripLastIndex).getNearestRdps());
                double firstIndexKm = ReportDistanceUtility.kmDistValue(current.get(0).getNearestRdps());

                if (firstIndexKm > startKM && lastIndexedValKM > endKM || lastIndexedValKM < endKMLess2M) {
                    log.info("11111111111111111");
                    log.info("First IndexKm: {}", firstIndexKm);
                    log.info("Last IndexKm: {}", lastIndexedValKM);
                    log.info("11111111111111111");
                    return true;
                }

                current = new ArrayList<>();

                int start = i - 5 + 1;
                for (int j = start; j <= i; j++) {
                    current.add(validHistories.get(j));
                }
                currentDir = dir;
                streak = 0;
                continue;
            }
            current.add(curr);
        }
        int tripLastIndex = current.size() - 1;
        double lastIndexedValKM = ReportDistanceUtility.kmDistValue(current.get(tripLastIndex).getNearestRdps());
        double firstIndexKm = ReportDistanceUtility.kmDistValue(current.get(0).getNearestRdps());
        if (firstIndexKm > startKM && lastIndexedValKM > endKM || lastIndexedValKM < endKMLess2M) {
            return true;
        }
        return false;
    }

    public boolean tripExistsBtoA(List<DeviceHistory> validHistories, Double startKm, Double endKm) {
        double startKM = endKm + 0.1;
        double endKM = startKm - 0.1;
        double endKMMore2MM = endKM + 0.02;

        int currentDir = 0;
        int streak = 0;

        List<DeviceHistory> current = new ArrayList<>();

        for (int i = validHistories.size() - 1; i > 0; i--) {

            DeviceHistory prev = validHistories.get(i - 1);
            DeviceHistory curr = validHistories.get(i);

            int dir = direction(prev, curr);

            if (currentDir == 0) {
                currentDir = dir;
            }

            if (dir == currentDir || dir == 0) {
                streak = 0;
                current.add(curr);
                continue;
            }

            streak++;

            if (streak >= 5) {
                int tripLastIndex = current.size() - 5;

                double lastIndexedValKM = ReportDistanceUtility
                        .kmDistValue(current.get(tripLastIndex).getNearestRdps());
                double firstIndexKm = ReportDistanceUtility.kmDistValue(current.get(0).getNearestRdps());

                if (firstIndexKm < startKM && lastIndexedValKM > endKM || lastIndexedValKM < endKMMore2MM) {
                    log.info("11111111111111111");
                    log.info("First IndexKm: {}", firstIndexKm);
                    log.info("Last IndexKm: {}", lastIndexedValKM);
                    log.info("11111111111111111");
                    return true;
                }

                current = new ArrayList<>();

                int start = i - 5 + 1;
                if (start > 0) {
                    for (int j = start; j <= i; j++) {
                        current.add(validHistories.get(j));
                    }
                }
                currentDir = dir;
                streak = 0;
                continue;
            }
            current.add(curr);
        }

        int tripLastIndex = current.size() - 1;
        double lastIndexedValKM = ReportDistanceUtility.kmDistValue(current.get(tripLastIndex).getNearestRdps());
        double firstIndexKm = ReportDistanceUtility.kmDistValue(current.get(0).getNearestRdps());
        if (firstIndexKm < startKM && lastIndexedValKM > endKM || lastIndexedValKM < endKMMore2MM) {
            return true;
        }
        return false;
    }

    public int direction(DeviceHistory a, DeviceHistory b) {
        double ka = ReportDistanceUtility.kmDistValue(a.getNearestRdps());
        double kb = ReportDistanceUtility.kmDistValue(b.getNearestRdps());

        if (kb > ka)
            return +1;
        if (kb < ka)
            return -1;
        return 0;
    }

    /// step 2.4: check overspeed
    public boolean checkOverspeed(List<DeviceHistory> histories) {
        DeviceHistory prev = null;
        for (int i = 0; i < histories.size(); i++) {

            if (prev != null
                    && histories.get(i).getGeoLocation().getCoordinates().get(1)
                            .equals(prev.getGeoLocation().getCoordinates().get(1))
                    && histories.get(i).getGeoLocation().getCoordinates().get(0)
                            .equals(prev.getGeoLocation().getCoordinates().get(0))) {
                continue;
            } else {
                if (histories.get(i).getSpeed() > 8) {
                    int j = i + 5;
                    int count = 0;
                    if (j >= histories.size())
                        return false;
                    for (int k = j; k > i; k--) {
                        if (histories.get(k).getSpeed() < 8) {
                            i = j;
                            count = 0;
                            break;
                        }
                        count++;
                    }
                    if (count >= 5) {
                        return true;
                    } else {
                        continue;
                    }
                }
            }
            prev = histories.get(i);
        }
        return false;
    }

}
