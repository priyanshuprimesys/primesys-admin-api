package com.primesys.adminservicemongodb.repository;

import com.primesys.adminservicemongodb.entity.IssueTicketEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueTicketRepository extends MongoRepository<IssueTicketEntity, String> {

    List<IssueTicketEntity> findByActiveStatusTrue();

    List<IssueTicketEntity> findByActiveStatusTrueAndIssueStatus(String issueStatus);

    List<IssueTicketEntity> findByActiveStatusTrueAndDivisionId(String divisionId);

    List<IssueTicketEntity> findByActiveStatusTrueAndGroupName(String groupName);

    List<IssueTicketEntity> findByActiveStatusTrueAndPriority(String priority);

    Optional<IssueTicketEntity> findByTicketId(String ticketId);

    Optional<IssueTicketEntity> findByNoteId(String noteId);

    Optional<IssueTicketEntity> findTopByOrderByTicketIdDesc();

    List<IssueTicketEntity> findByActiveStatusTrueAndAssignee(String assignee);

    List<IssueTicketEntity> findByActiveStatusTrueAndReporter(String reporter);

    List<IssueTicketEntity> findByActiveStatusTrueAndWatchersContaining(String userId);

    List<IssueTicketEntity> findByActiveStatusTrueAndResolution(String resolution);

    List<IssueTicketEntity> findByActiveStatusTrueAndDueDateIsNotNullAndDueDateLessThanAndIssueStatusNotIn(Long now,
            java.util.List<String> excludedStatuses);

    List<IssueTicketEntity> findByActiveStatusTrueAndDivisionIdAndDueDateIsNotNullAndDueDateLessThanAndIssueStatusNotIn(
            String divisionId, Long now, java.util.List<String> excludedStatuses);
}
