package com.primesys.adminserviceserver.modules.reports.services.impl;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.ReportEmailQueueEntity;
import com.primesys.adminservicemongodb.entity.ReportEmailQueueLogEntity;
import com.primesys.adminservicemongodb.enums.EmailStatus;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminservicemongodb.repository.ReportEmailQueueLogRepository;
import com.primesys.adminservicemongodb.repository.ReportEmailQueueRepository;
import com.primesys.adminserviceserver.modules.reports.dtos.AdminQueueViewDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailDTO;
import com.primesys.adminserviceserver.modules.reports.dtos.ReportEmailQueueLogDTO;
import com.primesys.adminserviceserver.modules.reports.mapper.ReportEmailMapper;
import com.primesys.adminserviceserver.modules.reports.mapper.ReportEmailQueueLogMapper;
import com.primesys.adminserviceserver.modules.reports.services.ReportEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportEmailServiceImpl implements ReportEmailService {

    private final ReportEmailQueueRepository reportEmailQueueRepository;
    private final ReportEmailQueueLogRepository reportEmailQueueLogRepository;
    private final DivisionLoginRepository divisionLoginRepository;

    @Override
    public List<ReportEmailDTO> getAllReportEmailStatus(Long reportDate) {
        List<ReportEmailQueueEntity> emailQueueEntities = reportEmailQueueRepository.findByReportDate(reportDate);

        if (emailQueueEntities.isEmpty()) {
            return List.of();
        }

        List<ReportEmailDTO> reportEmailDTOS = new ArrayList<>();

        List<Map<String, Object>> divisionList = new ArrayList<>();

        for (ReportEmailQueueEntity reportEmailQueueEntity : emailQueueEntities) {
            String divisionId = reportEmailQueueEntity.getDivisionId();

            DivisionLoginEntity division;

            if (divisionList.isEmpty()) {

                division = divisionLoginRepository.findById(divisionId)
                        .orElseThrow(() -> new IllegalArgumentException("No such division exists"));

                Map<String, Object> map = new HashMap<>();
                map.put("divisionId", divisionId);
                map.put("division", division);
                divisionList.add(map);
            } else {
                Optional<Map<String, Object>> existingDivision = divisionList.stream()
                        .filter(m -> divisionId.equals(m.get("divisionId"))).findFirst();

                if (existingDivision.isPresent()) {
                    division = (DivisionLoginEntity) existingDivision.get().get("division");
                } else {
                    division = divisionLoginRepository.findById(divisionId)
                            .orElseThrow(() -> new IllegalArgumentException("No such division exists"));

                    Map<String, Object> map = new HashMap<>();
                    map.put("divisionId", divisionId);
                    map.put("division", division);

                    divisionList.add(map);
                }
            }

            List<ReportEmailQueueLogEntity> reportEmailQueueLogEntities = reportEmailQueueLogRepository
                    .findByQueueIdAndReportDate(reportEmailQueueEntity.getId(), reportEmailQueueEntity.getReportDate());

            ReportEmailDTO dto = ReportEmailMapper.toDTO(reportEmailQueueEntity, division,
                    reportEmailQueueLogEntities.stream().map(ReportEmailQueueLogMapper::toDTO).toList());

            reportEmailDTOS.add(dto);
        }

        return reportEmailDTOS;
    }

    @Override
    public List<AdminQueueViewDTO> getAdminSystemView(Long reportDate, boolean includeLogs) {
        List<ReportEmailQueueEntity> queues = reportDate != null
                ? reportEmailQueueRepository.findByReportDateOrderByCreatedAtAsc(reportDate)
                : reportEmailQueueRepository.findAll();

        Map<String, String> divisionNameCache = new HashMap<>();

        return queues.stream().map(queue -> {
            String divisionName = divisionNameCache.computeIfAbsent(queue.getDivisionId(),
                    id -> divisionLoginRepository.findById(id).map(d -> d.getName()).orElse("Unknown"));

            List<ReportEmailQueueLogDTO> processLogs = includeLogs
                    ? reportEmailQueueLogRepository.findByQueueIdOrderByCreatedAtDesc(queue.getId()).stream()
                            .map(ReportEmailQueueLogMapper::toDTO).toList()
                    : null;

            return ReportEmailMapper.toAdminDTO(queue, divisionName, processLogs);
        }).toList();
    }

    @Override
    public List<ReportEmailQueueLogDTO> getProcessLogsByQueueId(String queueId) {
        return reportEmailQueueLogRepository.findByQueueIdOrderByCreatedAtDesc(queueId).stream()
                .map(ReportEmailQueueLogMapper::toDTO).toList();
    }

    @Override
    public List<ReportEmailQueueLogDTO> getActiveProcessLogs() {
        return reportEmailQueueLogRepository.findByStatus(EmailStatus.SENDING).stream()
                .map(ReportEmailQueueLogMapper::toDTO).toList();
    }

    @Override
    public List<ReportEmailQueueLogDTO> getProcessLogsByDivisionAndDate(String divisionId, Integer deviceTypeId,
            Long reportDate) {
        return reportEmailQueueLogRepository
                .findByDivisionIdAndDeviceTypeIdAndReportDate(divisionId, deviceTypeId, reportDate).stream()
                .map(ReportEmailQueueLogMapper::toDTO).toList();
    }

}
