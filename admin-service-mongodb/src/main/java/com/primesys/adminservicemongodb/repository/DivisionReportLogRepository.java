package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.DivisionReportLogEntity;
import com.primesys.adminservicemongodb.enums.StatusEnum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DivisionReportLogRepository extends MongoRepository<DivisionReportLogEntity, String> {

    List<DivisionReportLogEntity> findByDivisionId(String divisionId);

    DivisionReportLogEntity findByDivisionIdAndDeviceTypeIdAndReportDate(String division, int deviceTypeId,
            long reportDate);

    List<DivisionReportLogEntity> findAllByDivisionIdAndDeviceTypeIdAndReportDate(String divisionId,
            Integer deviceTypeId, Long reportDate);

    DivisionReportLogEntity findByDivisionIdAndDeviceTypeIdAndReportDateAndStatus(String divisionId,
            Integer deviceTypeId, Long reportDate, StatusEnum status);

    // DivisionReportLogEntity findByDivisionIdAndDeviceTypeIdAndReportDateAndStatus(
    // String divisionId,
    // Integer deviceTypeId,
    // Long reportDate,
    // StatusEnum status
    // );

    boolean existsByDivisionIdAndDeviceTypeIdAndReportDate(String divisionId, Integer deviceTypeId, Long reportDate);
}
