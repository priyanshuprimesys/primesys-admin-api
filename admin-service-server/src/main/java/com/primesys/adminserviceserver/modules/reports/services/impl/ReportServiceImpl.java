package com.primesys.adminserviceserver.modules.reports.services.impl;

import com.primesys.adminservicemongodb.entity.*;
import com.primesys.adminservicemongodb.enums.StatusEnum;
import com.primesys.adminservicemongodb.repository.*;
import com.primesys.adminserviceserver.modules.reports.dtos.*;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.DivisionReportConfigDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ModuleMasterDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ReportConfigDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.report_config_dto.ReportConfigDetailDTO;
import com.primesys.adminserviceserver.modules.reports.mapper.ReportConfigMapper;
import com.primesys.adminserviceserver.modules.reports.mapper.ReportLogMapper;
import com.primesys.adminserviceserver.modules.reports.mapper.TripReportStatusSummaryMapper;
import com.primesys.adminserviceserver.modules.reports.services.ReportService;
import com.primesys.adminserviceserver.utility.DateTimeUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final DivisionLoginRepository divisionLoginRepository;
    private final DeviceRepository deviceRepository;
    private final ModuleMasterRepository moduleMasterRepository;
    private final DivisionReportLogRepository divisionReportLogRepository;
    private final TripStatusReportSummaryRepository tripStatusReportSummaryRepository;

    @Override
    public ReportConfigDTO getConfig(String divisionId) {
        String divisionPathId = "," + divisionId + ",";
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository.findDeviceListByPath(divisionPathId);

        /// sorting according to hierarchy
        divisionLoginEntities.sort(Comparator
                .comparingInt(d -> (int) Arrays.stream(d.getPath().split(",")).filter(s -> !s.isBlank()).count()));
        /// parent report config
        ReportConfigDTO reportConfig = new ReportConfigDTO();
        /// child report config
        List<ReportConfigDetailDTO> reportConfigDetailDTOS = new ArrayList<>();
        int totalDeviceCount = 0;

        for (DivisionLoginEntity divisionLoginEntity : divisionLoginEntities) {
            /// for every division detail
            ReportConfigDetailDTO reportConfigDetailDTO = new ReportConfigDetailDTO();

            List<Integer> deviceNos = Arrays.stream(divisionLoginEntity.getDeviceList().split(",")).map(String::trim)
                    .filter(trim -> !trim.isEmpty() && !"null".equalsIgnoreCase(trim)).map(Integer::parseInt).toList();

            totalDeviceCount = totalDeviceCount + deviceNos.size();
            List<DeviceEntity> deviceEntities = deviceRepository.findByNoTrackDivId(divisionId, deviceNos);

            int active = deviceEntities.stream()
                    .filter(deviceEntity -> deviceEntity.getActiveStatus() == null || deviceEntity.getActiveStatus())
                    .toList().size();

            int inActive = deviceEntities.stream()
                    .filter(deviceEntity -> deviceEntity.getActiveStatus() == null || !deviceEntity.getActiveStatus())
                    .toList().size();

            int reportEnabled = deviceEntities.stream()
                    .filter(deviceEntity -> deviceEntity.getReportEnable() == null || deviceEntity.getReportEnable())
                    .toList().size();

            int reportDisabled = deviceEntities.stream()
                    .filter(deviceEntity -> deviceEntity.getReportEnable() == null || !deviceEntity.getReportEnable())
                    .toList().size();

            reportConfigDetailDTO.setDivisionId(divisionLoginEntity.getId());
            reportConfigDetailDTO.setName(divisionLoginEntity.getName());
            reportConfigDetailDTO.setUserName(divisionLoginEntity.getUserName());
            reportConfigDetailDTO.setTotalDevices(deviceNos.size());
            reportConfigDetailDTO.setActiveDevices(active);
            reportConfigDetailDTO.setReportEnabled(reportEnabled);
            reportConfigDetailDTO.setReportDisabled(reportDisabled);
            reportConfigDetailDTO.setInActiveDevices(inActive);
            reportConfigDetailDTO.setTotalReportModule(divisionLoginEntity.getModulesList().size());

            reportConfigDetailDTOS.add(reportConfigDetailDTO);

        }

        int totalActiveDevices = reportConfigDetailDTOS.stream().map(ReportConfigDetailDTO::getActiveDevices)
                .filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        int totalInActiveDevices = reportConfigDetailDTOS.stream().map(ReportConfigDetailDTO::getInActiveDevices)
                .filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        int reportInActiveDevices = reportConfigDetailDTOS.stream().map(ReportConfigDetailDTO::getReportDisabled)
                .filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        int reportEnabledDevices = reportConfigDetailDTOS.stream().map(ReportConfigDetailDTO::getReportEnabled)
                .filter(Objects::nonNull).mapToInt(Integer::intValue).sum();

        reportConfig.setTotalDevices(totalDeviceCount);
        reportConfig.setActiveDevices(totalActiveDevices);
        reportConfig.setInActiveDevices(totalInActiveDevices);
        reportConfig.setReportConfigDetail(reportConfigDetailDTOS);
        reportConfig.setReportDisabledDevices(reportInActiveDevices);
        reportConfig.setReportActiveDevices(reportEnabledDevices);

        return reportConfig;
    }

    @Override
    public DivisionReportConfigDTO getDivisionReportDetail(String divisionId) {
        DivisionLoginEntity divisionLogin = divisionLoginRepository.findById(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("Division not found"));

        DivisionReportConfigDTO divisionConfig = new DivisionReportConfigDTO();

        divisionConfig.setName(divisionLogin.getName());
        divisionConfig.setReportEnable(divisionLogin.getReportEnable());
        divisionConfig.setReportEmailId(divisionLogin.getReportEmailId());
        divisionConfig.setReportEmailPassword(divisionLogin.getReportEmailIdPassword());
        return divisionConfig;
    }

    @Override
    public List<ModuleMasterDTO> getReportModuleList(String divisionId) {
        DivisionLoginEntity divisionLogin = divisionLoginRepository.findById(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("Division not found"));

        List<ModuleMasterEntity> moduleMasterEntities = moduleMasterRepository.findAll();

        List<ModuleMasterDTO> moduleMasterDTOS = new ArrayList<>();

        for (ModuleMasterEntity moduleMasterEntity : moduleMasterEntities) {
            ModuleMasterDTO masterDTO = new ModuleMasterDTO();
            if (divisionLogin.getModulesList().contains(moduleMasterEntity.getId())) {
                masterDTO = ReportConfigMapper.toDTO(moduleMasterEntity, true);

            } else {
                masterDTO = ReportConfigMapper.toDTO(moduleMasterEntity, false);
            }
            moduleMasterDTOS.add(masterDTO);
        }
        return moduleMasterDTOS;
    }

    @Override
    public List<DivisionReportLogDTO> getDivisionReportLog(String divisionId, Integer deviceTypeId, Long reportDate) {

        List<DivisionReportLogEntity> divisionReportLogEntities = divisionReportLogRepository
                .findAllByDivisionIdAndDeviceTypeIdAndReportDate(divisionId, deviceTypeId, reportDate);

        return divisionReportLogEntities.stream().map(ReportLogMapper::toDto).toList();
    }

    @Override
    public String destroyReportLog(String reportId) {
        if (!divisionReportLogRepository.existsById(reportId)) {
            throw new IllegalArgumentException("Report does not exists");
        }
        divisionReportLogRepository.deleteById(reportId);
        return "Report Deleted successfully";
    }

    @Override
    public String updateReportLogStatus(String reportId, String divisionId, Integer deviceTypeId, Long reportDate,
            String status) {
        DivisionReportLogEntity divisionReportLog = divisionReportLogRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("No such report generated"));

        if (divisionReportLog.getPath() != null && !divisionReportLog.getPath().isBlank()
                || !Objects.requireNonNull(divisionReportLog.getPath()).isEmpty()) {
            List<String> reportIds = new ArrayList<>(Arrays.stream(divisionReportLog.getPath().split(",")).toList());

            reportIds.add(divisionReportLog.getId().toString());
            StatusEnum updatedStatus = StatusEnum.valueOf(status);
            /// get all report ids
            for (String report : reportIds) {
                log.info("executing for id {}", report);
                /// find report log one by one
                Optional<DivisionReportLogEntity> reportLog = divisionReportLogRepository.findById(report);
                /// if present do operation
                if (reportLog.isPresent()) {
                    DivisionReportLogEntity entity = reportLog.get();
                    /// search for logs generated
                    List<DivisionReportLogEntity> reportLogEntities = divisionReportLogRepository
                            .findAllByDivisionIdAndDeviceTypeIdAndReportDate(entity.getDivisionId(),
                                    entity.getDeviceTypeId(), entity.getReportDate());

                    if (reportLogEntities == null || reportLogEntities.isEmpty()) {
                        throw new RuntimeException("No report logs found");
                    }

                    /// if the log is there then expect the entity id disabled every other log
                    for (DivisionReportLogEntity reportLog1 : reportLogEntities) {
                        /// if found then update status
                        if (reportLog1.getId().equals(entity.getId())) {
                            reportLog1.setStatus(updatedStatus);

                        } else {
                            /// else inactive every report
                            reportLog1.setStatus(StatusEnum.INACTIVE);
                        }
                    }

                    divisionReportLogRepository.saveAll(reportLogEntities);

                }
            }
        } else {
            List<DivisionReportLogEntity> reportLogs = divisionReportLogRepository
                    .findAllByDivisionIdAndDeviceTypeIdAndReportDate(divisionId, deviceTypeId, reportDate);
            if (reportLogs == null || reportLogs.isEmpty()) {
                throw new RuntimeException("No report logs found");
            }

            StatusEnum updatedStatus = StatusEnum.valueOf(status);

            for (DivisionReportLogEntity reportLog : reportLogs) {

                if (reportLog.getId().toString().equals(reportId)) {

                    reportLog.setStatus(updatedStatus);

                } else if (updatedStatus == StatusEnum.ACTIVE) {
                    reportLog.setStatus(StatusEnum.INACTIVE);
                }
            }

            divisionReportLogRepository.saveAll(reportLogs);
        }

        return "Report status updated successfully";
    }

    @Override
    public String updateModuleList(String divisionId, List<String> updatedList) {
        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository.findByPath(divisionId);

        if (divisionLoginEntities.isEmpty()) {
            throw new IllegalArgumentException("No division exists with this Id");
        }

        for (DivisionLoginEntity divisionLoginEntity : divisionLoginEntities) {
            divisionLoginEntity.setModulesList(updatedList);
        }
        divisionLoginRepository.saveAll(divisionLoginEntities);
        return "Report Modules updated successfully";
    }

    @Override
    public String getRegeneratedReportLog(String divisionId, Integer deviceTypeId, Long reportDate) {
        try {

            String localUrl = "http://localhost:8080/user-service/exceptions/exception-regenerate-report"
                    + "?divisionId=" + divisionId + "&deviceType=" + deviceTypeId + "&startDateTime=" + reportDate;

            String prodUrl = "https://mykiddytracker.in/user-service/exceptions/exception-regenerate-report"
                    + "?divisionId=" + divisionId + "&deviceType=" + deviceTypeId + "&startDateTime=" + reportDate;

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.getForEntity(prodUrl, String.class);

            return response.getBody();

        } catch (Exception e) {

            log.error("Failed to call regenerate report API", e);

            return "FAILED";
        }
    }

    @Override
    public List<TripStatusSummaryDTO> getTripReportStatusSummary(String divisionId, Integer deviceType, Long startDate,
            Long endDate) {
        DivisionLoginEntity divisionLogin = divisionLoginRepository.findById(divisionId)
                .orElseThrow(() -> new IllegalArgumentException("Division does not exists"));
        long startHourDate = DateTimeUtility.dateToMidNightEpoch(startDate);
        long endHourDate = DateTimeUtility.dateToMidNightEpoch(endDate);
        List<TripStatusReportSummaryEntity> entities = tripStatusReportSummaryRepository
                .findByPathRegexAndReportOfTheDayBetweenAndDeviceType(divisionLogin.getPath(), startHourDate,
                        endHourDate, deviceType);
        // List<TripStatusSummaryReportDTO> reportDTOS =
        // entities.stream().map(TripReportStatusSummaryMapper::toDTO).toList();
        List<TripStatusSummaryDTO> tripStatusSummaryDTOS = new ArrayList<>();
        /// all device nos in list
        Set<Integer> allDeviceNos = new HashSet<>();

        for (TripStatusReportSummaryEntity reportDTO : entities) {
            List<Integer> offNos = Arrays.stream(reportDTO.getDeviceOff().split(",")).map(String::trim)
                    .filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
            List<Integer> delayedNos = Arrays.stream(reportDTO.getDelayedStart().split(",")).map(String::trim)
                    .filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
            List<Integer> tripNotCompleteNos = Arrays.stream(reportDTO.getTripNotCompleted().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
            List<Integer> tripCompletedNos = Arrays.stream(reportDTO.getTripCompleted().split(",")).map(String::trim)
                    .filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
            List<Integer> overSpeedNos = Arrays.stream(reportDTO.getOverSpeed().split(",")).map(String::trim)
                    .filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();

            allDeviceNos.addAll(offNos);
            allDeviceNos.addAll(delayedNos);
            allDeviceNos.addAll(tripNotCompleteNos);
            allDeviceNos.addAll(tripCompletedNos);
            allDeviceNos.addAll(overSpeedNos);
        }

        List<DeviceEntity> devices = deviceRepository.findByDivisionIdAndDeviceNoIn(divisionId,
                new ArrayList<>(allDeviceNos));

        log.info("device list {}", devices.size());

        Set<Long> reportDays = entities.stream().map(TripStatusReportSummaryEntity::getReportOfTheDay)
                .collect(Collectors.toSet());

        log.info("reprot days {}", reportDays.size());

        for (Long reportDay : reportDays) {

            List<TripStatusDTO> tripStatusDTOS = new ArrayList<>();
            List<TripStatusReportSummaryEntity> reportDayEntity = entities.stream()
                    .filter(t -> t.getReportOfTheDay() == reportDay).toList();

            for (TripStatusReportSummaryEntity reportSummary : reportDayEntity) {

                List<Integer> offNos = Arrays.stream(reportSummary.getDeviceOff().split(",")).map(String::trim)
                        .filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();

                for (Integer deviceNo : offNos) {
                    TripStatusDTO tripStatusDTO = new TripStatusDTO();
                    tripStatusDTO.setDeviceNo(deviceNo);
                    tripStatusDTO.setReportDate(reportDay);
                    tripStatusDTO.setStatus("OFF");
                    tripStatusDTOS.add(tripStatusDTO);
                }
                List<Integer> delayedNos = Arrays.stream(reportSummary.getDelayedStart().split(",")).map(String::trim)
                        .filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
                for (Integer deviceNo : delayedNos) {
                    TripStatusDTO tripStatusDTO = new TripStatusDTO();
                    tripStatusDTO.setDeviceNo(deviceNo);
                    tripStatusDTO.setReportDate(reportDay);
                    tripStatusDTO.setStatus("DELAY");
                    tripStatusDTOS.add(tripStatusDTO);
                }
                List<Integer> tripNotCompleteNos = Arrays.stream(reportSummary.getTripNotCompleted().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
                for (Integer deviceNo : tripNotCompleteNos) {
                    TripStatusDTO tripStatusDTO = new TripStatusDTO();
                    tripStatusDTO.setDeviceNo(deviceNo);
                    tripStatusDTO.setReportDate(reportDay);
                    tripStatusDTO.setStatus("NOT");
                    tripStatusDTOS.add(tripStatusDTO);
                }

                List<Integer> tripCompletedNos = Arrays.stream(reportSummary.getTripCompleted().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();

                for (Integer deviceNo : tripCompletedNos) {
                    TripStatusDTO tripStatusDTO = new TripStatusDTO();
                    tripStatusDTO.setDeviceNo(deviceNo);
                    tripStatusDTO.setReportDate(reportDay);
                    tripStatusDTO.setStatus("DONE");
                    tripStatusDTOS.add(tripStatusDTO);
                }
                List<Integer> overSpeedNos = Arrays.stream(reportSummary.getOverSpeed().split(",")).map(String::trim)
                        .filter(s -> !s.isEmpty()).map(Integer::parseInt).toList();
                for (Integer deviceNo : overSpeedNos) {
                    TripStatusDTO tripStatusDTO = new TripStatusDTO();
                    tripStatusDTO.setDeviceNo(deviceNo);
                    tripStatusDTO.setReportDate(reportDay);
                    tripStatusDTO.setStatus("SPEED");
                    tripStatusDTOS.add(tripStatusDTO);
                }

            }
            for (DeviceEntity device : devices) {
                List<TripStatusDTO> dev = tripStatusDTOS.stream()
                        .filter(tripStatusDTO -> Objects.equals(tripStatusDTO.getDeviceNo(), device.getDeviceNo()))
                        .toList();
                TripStatusSummaryDTO tripStatusSummaryDTO = new TripStatusSummaryDTO();
                tripStatusSummaryDTO.setDeviceName(device.buildDeviceName());
                tripStatusSummaryDTO.setDeviceType(deviceType);
                tripStatusSummaryDTO.setReportOfDay(reportDay);
                tripStatusSummaryDTO.setDeviceImei(device.getDeviceImei());
                tripStatusSummaryDTO.setDeviceNo(device.getDeviceNo());

                if (!dev.isEmpty()) {
                    for (TripStatusDTO trip : dev) {
                        if (trip.getStatus().toLowerCase().contains("OFF".toLowerCase())) {
                            tripStatusSummaryDTO.setDeviceOff(true);
                        }

                        if (trip.getStatus().toLowerCase().contains("SPEED".toLowerCase())) {
                            tripStatusSummaryDTO.setOverSpeed(true);
                        }

                        if (trip.getStatus().toLowerCase().contains("DONE".toLowerCase())) {
                            tripStatusSummaryDTO.setTripCompleted(true);
                        }

                        if (trip.getStatus().toLowerCase().contains("DELAY".toLowerCase())) {
                            tripStatusSummaryDTO.setDelayedStart(true);
                        }

                        if (trip.getStatus().toLowerCase().contains("NOT".toLowerCase())) {
                            tripStatusSummaryDTO.setTripNotCompleted(true);
                        }
                    }
                } else {
                    tripStatusSummaryDTO.setInActiveDevice(true);
                }
                tripStatusSummaryDTOS.add(tripStatusSummaryDTO);
            }
        }

        return tripStatusSummaryDTOS;
    }

    @Override
    public String getTripSummaryRegeneratedReport(String divisionId, Integer deviceType, Long reportDate) {

        try {
            String localUrl = "http://localhost:8080/user-service/exceptions/trip-summary-report-generate"
                    + "?divisionId=" + divisionId + "&deviceType=" + deviceType + "&reportDate=" + reportDate;

            String prodUrl = "https://mykiddytracker.in/user-service/exceptions/trip-summary-report-generate"
                    + "?divisionId=" + divisionId + "&deviceType=" + deviceType + "&reportDate=" + reportDate;

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.getForEntity(prodUrl, String.class);

            return response.getBody();
        } catch (Exception e) {

            log.error("Failed to call regenerate report API", e);

            return "FAILED";
        }
    }

    @Override
    public String destroyTripReportSummary(String divisionId, Integer deviceType, Long reportDate) {
        try {

            String localUrl = "http://localhost:8080/user-service/exceptions/destroy-trip-report-summary"
                    + "?divisionId=" + divisionId + "&deviceType=" + deviceType + "&reportDate=" + reportDate;

            String prodUrl = "https://mykiddytracker.in/user-service/exceptions/destroy-trip-report-summary"
                    + "?divisionId=" + divisionId + "&deviceType=" + deviceType + "&reportDate=" + reportDate;

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.getForEntity(prodUrl, String.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call destroy trip report API", e);
            return "FAILED";
        }
    }

    @Override
    public List<ReportEmailResponseDTO> getEmailReportLogs(String divisionId, Integer deviceTypeId, Long reportDate) {

        List<DivisionLoginEntity> divisionLoginEntities = divisionLoginRepository
                .findByPathContainingWithDeviceList(divisionId);

        List<ReportEmailResponseDTO> reportEmailResponseDTOS = new ArrayList<>();

        for (DivisionLoginEntity divisionLoginEntity : divisionLoginEntities) {
            DivisionReportLogEntity divisionReportLog = divisionReportLogRepository
                    .findByDivisionIdAndDeviceTypeIdAndReportDateAndStatus(divisionId, deviceTypeId, reportDate,
                            StatusEnum.ACTIVE);
            ReportEmailResponseDTO reportEmailResponseDTO = new ReportEmailResponseDTO();
            if (divisionReportLog == null) {
                reportEmailResponseDTO.setDivisionName(divisionLoginEntity.getName());
                reportEmailResponseDTO.setDivisionId("");
                reportEmailResponseDTO.setTrackDivisionId(divisionLoginEntity.getTrackDivisionId());
            } else {
                if (divisionReportLog.getReportEmail() != null) {
                    reportEmailResponseDTO.setReportEmailLog(divisionReportLog.getReportEmail());
                }
                reportEmailResponseDTO.setDivisionName(divisionLoginEntity.getName());
                reportEmailResponseDTO.setDivisionId(divisionReportLog.getDivisionId());
                reportEmailResponseDTO.setTrackDivisionId(divisionLoginEntity.getTrackDivisionId());
            }
            reportEmailResponseDTOS.add(reportEmailResponseDTO);

        }

        return reportEmailResponseDTOS;
    }

    @Override
    public String scheduleReportEmail(String divisionId, Integer deviceType, Long reportDate, String userId) {
        try {
            String localUrl = "http://localhost:8080/user-service/api/report-email/scheduleEmail";
            String prodUrl = "https://mykiddytracker.in/user-service/api/report-email/scheduleEmail";

            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("divisionId", divisionId);
            requestBody.put("deviceTypeId", deviceType);
            requestBody.put("reportDate", reportDate);
            requestBody.put("userId", userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(prodUrl, entity, String.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to call email send report API", e);
            return "FAILED";
        }
    }

}
