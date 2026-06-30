package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.ReportEmailQueueLogEntity;
import com.primesys.adminservicemongodb.enums.EmailStatus;
import com.primesys.adminservicemongodb.enums.ProcessType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportEmailQueueLogRepository extends MongoRepository<ReportEmailQueueLogEntity, String> {

    /** All log entries for a single queue run, newest first. */
    List<ReportEmailQueueLogEntity> findByQueueIdOrderByCreatedAtDesc(String queueId);

    List<ReportEmailQueueLogEntity> findByQueueIdAndReportDate(String queueId, Long reportDate);

    /** All log entries for a specific division node within a queue run. */
    List<ReportEmailQueueLogEntity> findByQueueIdAndProcessDivisionId(String queueId, String processDivisionId);

    /** All log entries for a specific operation type within a queue run. */
    List<ReportEmailQueueLogEntity> findByQueueIdAndProcessType(String queueId, ProcessType processType);

    /** Find a single in-progress entry to update it after the operation completes. */
    Optional<ReportEmailQueueLogEntity> findByQueueIdAndProcessDivisionIdAndProcessType(String queueId,
            String processDivisionId, ProcessType processType);

    /** All failed entries across all runs for a report date. */
    List<ReportEmailQueueLogEntity> findByDivisionIdAndDeviceTypeIdAndReportDateAndStatus(String divisionId,
            Integer deviceTypeId, Long reportDate, EmailStatus status);

    List<ReportEmailQueueLogEntity> findByDivisionIdAndDeviceTypeIdAndReportDate(String divisionId,
            Integer deviceTypeId, Long reportDate);

    List<ReportEmailQueueLogEntity> findByStatus(EmailStatus status);
}
