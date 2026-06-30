package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.ReportEmailQueueEntity;
import com.primesys.adminservicemongodb.enums.EmailStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportEmailQueueRepository extends MongoRepository<ReportEmailQueueEntity, String> {
    boolean existsByDivisionIdAndDeviceTypeIdAndReportEndTime(String divisionId, Integer deviceTypeId,
            Long reportEndTime);

    ReportEmailQueueEntity findByDivisionId(String divisionId);

    List<ReportEmailQueueEntity> findBySentFalseOrderByCreatedAtAsc();

    List<ReportEmailQueueEntity> findByReportDate(Long reportDate);

    List<ReportEmailQueueEntity> findByReportEndTime(Long reportDate);

    List<ReportEmailQueueEntity> findByReportExtendedEndTimeLessThanAndSentFalse(Long currentEpoch);

    boolean existsByDivisionIdAndDeviceTypeIdAndReportDate(String divisionId, Integer deviceTypeId, Long reportDate);

    List<ReportEmailQueueEntity> findByStatus(EmailStatus status);

    List<ReportEmailQueueEntity> findByDivisionIdAndDeviceTypeIdAndReportDate(String divisionId, Integer deviceTypeId,
            Long reportDate);

    List<ReportEmailQueueEntity> findByReportDateOrderByCreatedAtAsc(Long reportDate);
}
