package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicemongodb.entity.*;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminservicemongodb.repository.IssueSkipMsgRepository;
import com.primesys.adminservicemongodb.repository.IssueTicketRepository;
import com.primesys.adminservicemongodb.repository.IssueTransferAuditRepository;
import com.primesys.adminserviceserver.dtos.issue.ActivityEntry;
import com.primesys.adminserviceserver.dtos.issue.TicketStatsDto;
import com.primesys.adminserviceserver.dtos.issue.TransferMemberDto;
import com.primesys.adminserviceserver.request.*;
import com.primesys.adminserviceserver.service.IssueTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueTicketServiceImpl implements IssueTicketService {

    private final IssueTicketRepository issueTicketRepository;
    private final IssueTransferAuditRepository issueTransferAuditRepository;
    private final DivisionLoginRepository divisionLoginRepository;
    private final IssueSkipMsgRepository issueSkipMsgRepository;
    private final MongoTemplate mongoTemplate;

    private static final List<Integer> TRANSFER_ROLE_IDS = List.of(19, 20);
    private static final List<String> TERMINAL_STATUSES = List.of("RESOLVED", "CLOSED");

    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
            .withZone(ZoneId.of("UTC"));

    // ─── Read ────────────────────────────────────────────────────────────────

    @Override
    public List<IssueTicketEntity> getAllTickets() {
        return issueTicketRepository.findByActiveStatusTrue();
    }

    @Override
    public Optional<IssueTicketEntity> getTicketById(String id) {
        return issueTicketRepository.findById(id);
    }

    @Override
    public Optional<IssueTicketEntity> getTicketByTicketId(String ticketId) {
        return issueTicketRepository.findByTicketId(ticketId);
    }

    @Override
    public List<IssueTicketEntity> getTicketsByStatus(String status) {
        return issueTicketRepository.findByActiveStatusTrueAndIssueStatus(status);
    }

    @Override
    public List<IssueTicketEntity> getTicketsByDivision(String divisionId) {
        return issueTicketRepository.findByActiveStatusTrueAndDivisionId(divisionId);
    }

    @Override
    public List<IssueTicketEntity> getTicketsByGroup(String groupName) {
        return issueTicketRepository.findByActiveStatusTrueAndGroupName(groupName);
    }

    @Override
    public List<IssueTicketEntity> getTicketsByPriority(String priority) {
        return issueTicketRepository.findByActiveStatusTrueAndPriority(priority);
    }

    @Override
    public List<IssueTicketEntity> getTicketsByAssignee(String assignee) {
        return issueTicketRepository.findByActiveStatusTrueAndAssignee(assignee);
    }

    @Override
    public List<IssueTicketEntity> getTicketsByReporter(String reporter) {
        return issueTicketRepository.findByActiveStatusTrueAndReporter(reporter);
    }

    @Override
    public List<IssueTicketEntity> getTicketsByWatcher(String userId) {
        return issueTicketRepository.findByActiveStatusTrueAndWatchersContaining(userId);
    }

    // ─── Search / Paged ──────────────────────────────────────────────────────

    @Override
    public List<IssueTicketEntity> searchTickets(String status, String priority, String assignee, String divisionId,
            String group, String reporter, Long from, Long to) {
        Criteria criteria = Criteria.where("active_status").is(true);
        if (status != null && !status.isBlank()) {
            // support comma-separated values: OPEN,IN_PROGRESS
            List<String> statuses = Arrays.asList(status.split(","));
            criteria = criteria.and("issue_status").in(statuses);
        }
        if (priority != null && !priority.isBlank())
            criteria = criteria.and("priority").is(priority);
        if (assignee != null && !assignee.isBlank())
            criteria = criteria.and("assignee").is(assignee);
        if (divisionId != null && !divisionId.isBlank())
            criteria = criteria.and("division_id").is(divisionId);
        if (group != null && !group.isBlank())
            criteria = criteria.and("group_name").is(group);
        if (reporter != null && !reporter.isBlank())
            criteria = criteria.and("reporter").is(reporter);
        if (from != null)
            criteria = criteria.and("post_time").gte(from);
        if (to != null)
            criteria = criteria.and("post_time").lte(to);

        Query query = new Query(criteria).with(Sort.by(Sort.Direction.DESC, "post_time"));
        return mongoTemplate.find(query, IssueTicketEntity.class);
    }

    @Override
    public Page<IssueTicketEntity> getPagedTickets(String status, String priority, String assignee, String divisionId,
            Pageable pageable) {
        Criteria criteria = Criteria.where("active_status").is(true);
        if (status != null && !status.isBlank()) {
            List<String> statuses = Arrays.asList(status.split(","));
            criteria = criteria.and("issue_status").in(statuses);
        }
        if (priority != null && !priority.isBlank())
            criteria = criteria.and("priority").is(priority);
        if (assignee != null && !assignee.isBlank())
            criteria = criteria.and("assignee").is(assignee);
        if (divisionId != null && !divisionId.isBlank())
            criteria = criteria.and("division_id").is(divisionId);

        Query query = new Query(criteria).with(pageable);
        long total = mongoTemplate.count(new Query(criteria), IssueTicketEntity.class);
        List<IssueTicketEntity> content = mongoTemplate.find(query, IssueTicketEntity.class);
        return new PageImpl<>(content, pageable, total);
    }

    // ─── Create ──────────────────────────────────────────────────────────────

    @Override
    public IssueTicketEntity createTicket(CreateIssueTicketRequest request) {
        String ticketId = generateNextTicketId();
        String now = TIMESTAMP_FMT.format(Instant.now());

        StatusChangeLog openLog = new StatusChangeLog("OPEN", request.getCreatedBy(), System.currentTimeMillis());

        IssueTicketEntity entity = IssueTicketEntity.builder().ticketId(ticketId).message(request.getMessage())
                .groupName(request.getGroupName()).senderName(request.getSenderName())
                .sender(request.getSender() != null ? request.getSender() : "")
                .deviceImei(request.getDeviceImei() != null ? request.getDeviceImei() : "0")
                .divisionId(request.getDivisionId()).sourceMessageId(request.getSourceMessageId())
                .noteId(request.getNoteId())
                .postTime(request.getPostTime() != null ? request.getPostTime() : System.currentTimeMillis())
                .category(request.getCategory()).context(request.getContext())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .summary(request.getSummary()).suggestedAction(request.getSuggestedAction())
                .classifiedBy(request.getClassifiedBy() != null ? request.getClassifiedBy() : "manual")
                .assignee(request.getAssignee()).assigneeName(request.getAssigneeName()).reporter(request.getReporter())
                .reporterName(request.getReporterName()).createdBy(request.getCreatedBy())
                .affectedDevices(
                        request.getAffectedDevices() != null ? request.getAffectedDevices() : new ArrayList<>())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .attachments(request.getAttachments() != null ? request.getAttachments() : new ArrayList<>())
                .watchers(request.getWatchers() != null ? request.getWatchers() : new ArrayList<>())
                .watcherNames(new ArrayList<>())
                .linkedTickets(request.getLinkedTickets() != null ? request.getLinkedTickets() : new ArrayList<>())
                .comments(new ArrayList<>()).commentAuditLog(new ArrayList<>()).transferHistory(new ArrayList<>())
                .dueDate(request.getDueDate()).isIssue(true).issueStatus("OPEN").activeStatus(true).createdAt(now)
                .statusHistory(new ArrayList<>(List.of(openLog))).build();

        IssueTicketEntity saved = issueTicketRepository.save(entity);
        log.info("Created issue ticket: {}", saved.getTicketId());
        return saved;
    }

    // ─── Update ──────────────────────────────────────────────────────────────

    @Override
    public IssueTicketEntity updateTicket(UpdateIssueTicketRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.getId()));

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update();

        String now = TIMESTAMP_FMT.format(Instant.now());
        update.set("last_updated", now);

        if (request.getIssueStatus() != null && !request.getIssueStatus().equals(existing.getIssueStatus())) {
            update.set("issue_status", request.getIssueStatus());
            StatusChangeLog statusLog = new StatusChangeLog(request.getIssueStatus(), request.getUpdatedBy(),
                    System.currentTimeMillis());
            update.push("status_history", statusLog);
            update.push("comments", botComment(request.getUpdatedBy(),
                    "Status changed from " + existing.getIssueStatus() + " to " + request.getIssueStatus() + "."));

            if ("RESOLVED".equals(request.getIssueStatus()))
                update.set("resolved_at", System.currentTimeMillis());
            if ("CLOSED".equals(request.getIssueStatus()))
                update.set("closed_at", System.currentTimeMillis());
        }
        if (request.getPriority() != null && !request.getPriority().equals(existing.getPriority())) {
            update.set("priority", request.getPriority());
            update.push("comments", botComment(request.getUpdatedBy(),
                    "Priority changed from " + existing.getPriority() + " to " + request.getPriority() + "."));
        } else if (request.getPriority() != null) {
            update.set("priority", request.getPriority());
        }
        if (request.getCategory() != null)
            update.set("category", request.getCategory());
        if (request.getContext() != null)
            update.set("context", request.getContext());
        if (request.getSummary() != null)
            update.set("summary", request.getSummary());
        if (request.getSuggestedAction() != null)
            update.set("suggested_action", request.getSuggestedAction());
        if (request.getHumanLabel() != null)
            update.set("human_label", request.getHumanLabel());
        if (request.getAssignee() != null && !request.getAssignee().equals(existing.getAssignee())) {
            update.set("assignee", request.getAssignee());
            update.push("comments", botComment(request.getUpdatedBy(), "Assignee changed to "
                    + (request.getAssigneeName() != null ? request.getAssigneeName() : request.getAssignee()) + "."));
        } else if (request.getAssignee() != null) {
            update.set("assignee", request.getAssignee());
        }
        if (request.getAssigneeName() != null)
            update.set("assignee_name", request.getAssigneeName());
        if (request.getDivisionId() != null)
            update.set("division_id", request.getDivisionId());
        if (request.getDueDate() != null && !request.getDueDate().equals(existing.getDueDate())) {
            update.set("due_date", request.getDueDate());
            update.push("comments", botComment(request.getUpdatedBy(), "Due date updated."));
        } else if (request.getDueDate() != null) {
            update.set("due_date", request.getDueDate());
        }
        if (request.getDeviceImei() != null)
            update.set("device_imei", request.getDeviceImei());
        if (request.getResolution() != null)
            update.set("resolution", request.getResolution());
        if (request.getAffectedDevices() != null)
            update.set("affected_devices", request.getAffectedDevices());
        if (request.getTags() != null)
            update.set("tags", request.getTags());
        if (request.getAttachments() != null)
            update.set("attachments", request.getAttachments());
        if (request.getLinkedTickets() != null)
            update.set("linked_tickets", request.getLinkedTickets());

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        return issueTicketRepository.findById(request.getId()).orElse(existing);
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    public IssueTicketEntity changeStatus(String id, ChangeStatusRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String newStatus = request.getStatus();
        String oldStatus = existing.getIssueStatus();
        String now = TIMESTAMP_FMT.format(Instant.now());

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().set("issue_status", newStatus).set("last_updated", now).push("status_history",
                new StatusChangeLog(newStatus, request.getUpdatedBy(), System.currentTimeMillis()));

        String botMsg = request.getNote() != null && !request.getNote().isBlank()
                ? "Status changed from " + oldStatus + " to " + newStatus + ". Note: " + request.getNote()
                : "Status changed from " + oldStatus + " to " + newStatus + ".";
        update.push("comments", botComment(request.getUpdatedBy(), botMsg));

        if ("RESOLVED".equals(newStatus))
            update.set("resolved_at", System.currentTimeMillis());
        if ("CLOSED".equals(newStatus))
            update.set("closed_at", System.currentTimeMillis());

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Ticket {} status changed {} → {} by {}", existing.getTicketId(), oldStatus, newStatus,
                request.getUpdatedBy());
        return issueTicketRepository.findById(id).orElse(existing);
    }

    @Override
    public IssueTicketEntity resolveTicket(String id, ResolveTicketRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String now = TIMESTAMP_FMT.format(Instant.now());
        long ts = System.currentTimeMillis();

        String botMsg = request.getNote() != null && !request.getNote().isBlank()
                ? "Ticket resolved. Resolution: " + request.getResolution() + ". " + request.getNote()
                : "Ticket resolved. Resolution: " + request.getResolution() + ".";

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().set("issue_status", "RESOLVED").set("resolution", request.getResolution())
                .set("resolved_at", ts).set("last_updated", now)
                .push("status_history", new StatusChangeLog("RESOLVED", request.getResolvedBy(), ts))
                .push("comments", botComment(request.getResolvedBy(), botMsg));

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Ticket {} resolved by {} with resolution {}", existing.getTicketId(), request.getResolvedBy(),
                request.getResolution());
        return issueTicketRepository.findById(id).orElse(existing);
    }

    @Override
    public IssueTicketEntity closeTicket(String id, CloseTicketRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String now = TIMESTAMP_FMT.format(Instant.now());
        long ts = System.currentTimeMillis();

        String botMsg = request.getNote() != null && !request.getNote().isBlank()
                ? "Ticket closed. " + request.getNote() : "Ticket closed.";

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().set("issue_status", "CLOSED").set("closed_at", ts).set("last_updated", now)
                .push("status_history", new StatusChangeLog("CLOSED", request.getClosedBy(), ts))
                .push("comments", botComment(request.getClosedBy(), botMsg));

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Ticket {} closed by {}", existing.getTicketId(), request.getClosedBy());
        return issueTicketRepository.findById(id).orElse(existing);
    }

    @Override
    public IssueTicketEntity reopenTicket(String id, ReopenTicketRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String newStatus = existing.getAssignee() != null && !existing.getAssignee().isBlank() ? "IN_PROGRESS" : "OPEN";
        String now = TIMESTAMP_FMT.format(Instant.now());
        long ts = System.currentTimeMillis();

        String botMsg = request.getNote() != null && !request.getNote().isBlank()
                ? "Ticket reopened. " + request.getNote() : "Ticket reopened.";

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().set("issue_status", newStatus).unset("resolution").unset("resolved_at")
                .unset("closed_at").set("last_updated", now)
                .push("status_history", new StatusChangeLog(newStatus, request.getReopenedBy(), ts))
                .push("comments", botComment(request.getReopenedBy(), botMsg));

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Ticket {} reopened → {} by {}", existing.getTicketId(), newStatus, request.getReopenedBy());
        return issueTicketRepository.findById(id).orElse(existing);
    }

    // ─── Assignment ──────────────────────────────────────────────────────────

    @Override
    public String pickupTicket(String id, PickupTicketRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No Issue found with this IssueID"));

        if (existing.getAssignee() != null && !existing.getAssignee().isBlank()
                && !existing.getAssignee().equals(request.getUserId())) {
            return "Issue has been already picked by " + existing.getAssigneeName() + ".";
        }

        String now = TIMESTAMP_FMT.format(Instant.now());
        long ts = System.currentTimeMillis();

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().set("assignee", request.getUserId()).set("assignee_name", request.getUserName())
                .set("last_updated", now);

        // record the pickup event itself (picked-by = userId, picked-at = ts) on every pickup
        StatusChangeLog pickedUpLog = new StatusChangeLog("PICKED_UP", request.getUserId(), ts);

        if ("OPEN".equals(existing.getIssueStatus())) {
            StatusChangeLog inProgressLog = new StatusChangeLog("IN_PROGRESS", request.getUserId(), ts);
            update.set("issue_status", "IN_PROGRESS").push("status_history").each(pickedUpLog, inProgressLog);
        } else {
            update.push("status_history", pickedUpLog);
        }

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Ticket {} picked up by {}", existing.getTicketId(), request.getUserId());
        return "Issue has been picked up successfully";
    }

    @Override
    public IssueTicketEntity unassignTicket(String id, String unassignedBy) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String prevAssigneeName = existing.getAssigneeName() != null ? existing.getAssigneeName()
                : existing.getAssignee() != null ? existing.getAssignee() : "unknown";

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().unset("assignee").unset("assignee_name").set("last_updated", now).push("comments",
                botComment(unassignedBy, "Ticket unassigned (was assigned to " + prevAssigneeName + ")."));

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Ticket {} unassigned by {}", existing.getTicketId(), unassignedBy);
        return issueTicketRepository.findById(id).orElse(existing);
    }

    // ─── Transfer ────────────────────────────────────────────────────────────

    @Override
    public IssueTicketEntity transferTicket(TransferTicketRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.getId()));

        String fromAssignee = existing.getAssignee();
        String fromName = existing.getAssigneeName();
        String now = TIMESTAMP_FMT.format(Instant.now());
        long timestamp = System.currentTimeMillis();

        TransferLog transferLog = TransferLog.builder().from(fromAssignee).fromName(fromName)
                .to(request.getToAssignee()).toName(request.getToAssigneeName())
                .transferredBy(request.getTransferredBy()).reason(request.getReason()).transferredAt(timestamp).build();

        IssueTransferAudit audit = IssueTransferAudit.builder().issueId(existing.getId()).fromAssignee(fromAssignee)
                .toAssignee(request.getToAssignee()).transferredBy(request.getTransferredBy()).transferredAt(timestamp)
                .transferReason(request.getReason()).build();
        issueTransferAuditRepository.save(audit);

        Comment transferComment = Comment.create(
                String.format("Ticket transferred from %s to %s. Reason: %s",
                        fromName != null ? fromName : (fromAssignee != null ? fromAssignee : "unassigned"),
                        request.getToAssigneeName() != null ? request.getToAssigneeName() : request.getToAssignee(),
                        request.getReason() != null ? request.getReason() : "—"),
                request.getTransferredBy(), "SYSTEM", null);

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().set("assignee", request.getToAssignee())
                .set("assignee_name", request.getToAssigneeName()).set("last_updated", now)
                .push("transfer_history", transferLog).push("comments", transferComment);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Transferred ticket {} from {} to {}", existing.getTicketId(), fromAssignee, request.getToAssignee());
        return issueTicketRepository.findById(request.getId()).orElse(existing);
    }

    @Override
    public List<TransferLog> getTransferHistory(String ticketId) {
        IssueTicketEntity existing = issueTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        List<TransferLog> history = existing.getTransferHistory();
        if (history == null || history.isEmpty())
            return List.of();

        return history.stream().sorted((a, b) -> Long.compare(b.getTransferredAt() != null ? b.getTransferredAt() : 0L,
                a.getTransferredAt() != null ? a.getTransferredAt() : 0L)).toList();
    }

    @Override
    public List<TransferMemberDto> getTransferMembers() {
        return divisionLoginRepository.findByRoleIdInAndActiveStatusTrue(TRANSFER_ROLE_IDS).stream()
                .map(TransferMemberDto::from).toList();
    }

    // ─── Comments ────────────────────────────────────────────────────────────

    @Override
    public IssueTicketEntity addComment(AddCommentRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.getIssueId()));

        String role = request.getRole() != null ? request.getRole() : "USER";
        Comment comment = Comment.create(request.getMessage(), request.getCommentedBy(), role, null);

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().push("comments", comment).set("last_updated", now);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        return issueTicketRepository.findById(request.getIssueId()).orElse(existing);
    }

    @Override
    public IssueTicketEntity editComment(EditCommentRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.getIssueId()));

        String oldMessage = existing.getComments() == null ? "" : existing.getComments().stream()
                .filter(c -> request.getCommentId().equals(c.getId())).map(Comment::getMessage).findFirst().orElse("");

        CommentAuditLog auditLog = new CommentAuditLog(request.getCommentId(), "EDIT", request.getUpdatedBy(),
                System.currentTimeMillis(), oldMessage, request.getUpdatedMessage());

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(
                Criteria.where("_id").is(existing.getId()).and("comments.id").is(request.getCommentId()));
        Update update = new Update().set("comments.$.message", request.getUpdatedMessage()).set("last_updated", now)
                .push("comment_audit_log", auditLog);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        return issueTicketRepository.findById(request.getIssueId()).orElse(existing);
    }

    @Override
    public IssueTicketEntity deleteComment(DeleteCommentRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.getIssueId()));

        String oldMessage = existing.getComments() == null ? "" : existing.getComments().stream()
                .filter(c -> request.getCommentId().equals(c.getId())).map(Comment::getMessage).findFirst().orElse("");

        CommentAuditLog auditLog = new CommentAuditLog(request.getCommentId(), "DELETE", request.getDeletedBy(),
                System.currentTimeMillis(), oldMessage, null);

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().pull("comments", new org.bson.Document("id", request.getCommentId()))
                .push("comment_audit_log", auditLog).set("last_updated", now);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        return issueTicketRepository.findById(request.getIssueId()).orElse(existing);
    }

    // ─── Watch / Unwatch ─────────────────────────────────────────────────────

    @Override
    public IssueTicketEntity watchTicket(String id, String userId, String displayName) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        if (existing.getWatchers() != null && existing.getWatchers().contains(userId))
            return existing;

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().push("watchers", userId).set("last_updated", now);
        if (displayName != null)
            update.push("watcher_names", displayName);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        return issueTicketRepository.findById(id).orElse(existing);
    }

    @Override
    public IssueTicketEntity unwatchTicket(String id, String userId) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().pull("watchers", userId).set("last_updated", now);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        return issueTicketRepository.findById(id).orElse(existing);
    }

    // ─── Linked Tickets ──────────────────────────────────────────────────────

    @Override
    public IssueTicketEntity linkTicket(String id, LinkTicketRequest request) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        if (existing.getLinkedTickets() != null && existing.getLinkedTickets().contains(request.getLinkedTicketId()))
            return existing;

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().push("linked_tickets", request.getLinkedTicketId()).set("last_updated", now)
                .push("comments", botComment(request.getLinkedBy(), "Linked to " + request.getLinkedTicketId()
                        + (request.getLinkType() != null ? " (" + request.getLinkType() + ")" : "") + "."));

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Ticket {} linked to {}", id, request.getLinkedTicketId());
        return issueTicketRepository.findById(id).orElse(existing);
    }

    @Override
    public IssueTicketEntity unlinkTicket(String id, String linkedTicketId) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().pull("linked_tickets", linkedTicketId).set("last_updated", now);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Ticket {} unlinked from {}", id, linkedTicketId);
        return issueTicketRepository.findById(id).orElse(existing);
    }

    // ─── Attachments ─────────────────────────────────────────────────────────

    @Override
    public List<String> getAttachments(String id) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        return existing.getAttachments() != null ? existing.getAttachments() : List.of();
    }

    // private static final String ATTACHMENT_DIR = "D:\\issue_attachments";
    private static final String ATTACHMENT_DIR = "/home/issue_attachments";

    @Override
    public String uploadAttachment(String ticketId, MultipartFile file, String updatedBy) {
        IssueTicketEntity existing = issueTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        String originalName = file.getOriginalFilename();
        String baseName = FilenameUtils.getBaseName(originalName);
        String normalizedBaseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "_");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = normalizedBaseName + "_" + timestamp;

        File directory = new File(ATTACHMENT_DIR);
        if (!directory.exists())
            directory.mkdirs();

        File serverFile = new File(directory.getAbsolutePath() + File.separator + fileName + "."
                + FilenameUtils.getExtension(originalName));
        try {
            file.transferTo(serverFile);
        } catch (Exception e) {
            log.error("Error while uploading file: {} :: error message: {}", originalName, e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        String filePath = "issue_attachments/" + serverFile.getName();
        String now = TIMESTAMP_FMT.format(Instant.now());

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().push("attachments", filePath).set("last_updated", now);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Attachment uploaded for ticket {}: {}", existing.getTicketId(), filePath);
        return filePath;
    }

    @Override
    public IssueTicketEntity deleteAttachment(String id, String fileName, String deletedBy) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String now = TIMESTAMP_FMT.format(Instant.now());
        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().pull("attachments", fileName).set("last_updated", now).push("comments",
                botComment(deletedBy, "Attachment removed: " + fileName + "."));

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);

        // best-effort disk delete
        try {
            new File(ATTACHMENT_DIR + File.separator + fileName.replace("issue_attachments/", "")).delete();
        } catch (Exception e) {
            log.warn("Could not delete attachment file {}: {}", fileName, e.getMessage());
        }

        log.info("Attachment {} removed from ticket {} by {}", fileName, id, deletedBy);
        return issueTicketRepository.findById(id).orElse(existing);
    }

    // ─── Activity ────────────────────────────────────────────────────────────

    @Override
    public List<ActivityEntry> getActivity(String id) {
        IssueTicketEntity t = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        List<ActivityEntry> entries = new ArrayList<>();

        // Status history
        if (t.getStatusHistory() != null) {
            for (StatusChangeLog s : t.getStatusHistory()) {
                entries.add(ActivityEntry.builder().type("STATUS_CHANGE").timestamp(s.getChangedAt())
                        .actor(s.getChangedBy()).description("Status changed to " + s.getStatus()).build());
            }
        }

        // Comments (user + bot)
        if (t.getComments() != null) {
            for (Comment c : t.getComments()) {
                Long ts = c.getCommentedAt();
                String type = "SYSTEM".equals(c.getRole()) ? "BOT_COMMENT" : "COMMENT";
                entries.add(ActivityEntry.builder().type(type).timestamp(ts).actor(c.getCommentedBy())
                        .description(c.getMessage()).build());
            }
        }

        // Transfer history
        if (t.getTransferHistory() != null) {
            for (TransferLog tr : t.getTransferHistory()) {
                String desc = String.format("Transferred from %s to %s",
                        tr.getFromName() != null ? tr.getFromName() : tr.getFrom(),
                        tr.getToName() != null ? tr.getToName() : tr.getTo());
                if (tr.getReason() != null)
                    desc += ". Reason: " + tr.getReason();
                entries.add(ActivityEntry.builder().type("TRANSFER").timestamp(tr.getTransferredAt())
                        .actor(tr.getTransferredBy()).description(desc).build());
            }
        }

        // Comment audit log (edits & deletes)
        if (t.getCommentAuditLog() != null) {
            for (CommentAuditLog al : t.getCommentAuditLog()) {
                String type = "EDIT".equals(al.getAction()) ? "COMMENT_EDIT" : "COMMENT_DELETE";
                entries.add(ActivityEntry.builder().type(type).timestamp(al.getPerformedAt()).actor(al.getPerformedBy())
                        .description("Comment " + al.getAction().toLowerCase() + "d")
                        .details(Map.of("commentId", al.getCommentId())).build());
            }
        }

        // Sort newest first, nulls last
        entries.sort((a, b) -> {
            long ta = a.getTimestamp() != null ? a.getTimestamp() : 0L;
            long tb = b.getTimestamp() != null ? b.getTimestamp() : 0L;
            return Long.compare(tb, ta);
        });

        return entries;
    }

    // ─── Stats / Dashboard ───────────────────────────────────────────────────

    @Override
    public TicketStatsDto getStats(String divisionId) {
        List<IssueTicketEntity> tickets = divisionId != null && !divisionId.isBlank()
                ? issueTicketRepository.findByActiveStatusTrueAndDivisionId(divisionId)
                : issueTicketRepository.findByActiveStatusTrue();

        Map<String, Long> byStatus = tickets.stream().filter(t -> t.getIssueStatus() != null)
                .collect(Collectors.groupingBy(IssueTicketEntity::getIssueStatus, Collectors.counting()));

        Map<String, Long> byPriority = tickets.stream().filter(t -> t.getPriority() != null)
                .collect(Collectors.groupingBy(IssueTicketEntity::getPriority, Collectors.counting()));

        Map<String, Long> byAssignee = tickets.stream().filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssigneeName() != null ? t.getAssigneeName() : t.getAssignee(),
                        Collectors.counting()));

        return TicketStatsDto.builder().total(tickets.size()).byStatus(byStatus).byPriority(byPriority)
                .byAssignee(byAssignee).build();
    }

    @Override
    public List<IssueTicketEntity> getOverdueTickets(String divisionId) {
        long now = System.currentTimeMillis();
        List<String> excluded = TERMINAL_STATUSES;
        if (divisionId != null && !divisionId.isBlank()) {
            return issueTicketRepository
                    .findByActiveStatusTrueAndDivisionIdAndDueDateIsNotNullAndDueDateLessThanAndIssueStatusNotIn(
                            divisionId, now, excluded);
        }
        return issueTicketRepository.findByActiveStatusTrueAndDueDateIsNotNullAndDueDateLessThanAndIssueStatusNotIn(now,
                excluded);
    }

    // ─── Bulk ────────────────────────────────────────────────────────────────

    @Override
    public List<IssueTicketEntity> bulkUpdateStatus(BulkStatusRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty())
            return List.of();

        String now = TIMESTAMP_FMT.format(Instant.now());
        long ts = System.currentTimeMillis();
        StatusChangeLog statusLog = new StatusChangeLog(request.getStatus(), request.getUpdatedBy(), ts);
        Comment bot = botComment(request.getUpdatedBy(), "Status changed to " + request.getStatus() + ".");

        for (String id : request.getIds()) {
            try {
                Query query = new Query(Criteria.where("_id").is(id));
                Update update = new Update().set("issue_status", request.getStatus()).set("last_updated", now)
                        .push("status_history", statusLog).push("comments", bot);
                if ("RESOLVED".equals(request.getStatus()))
                    update.set("resolved_at", ts);
                if ("CLOSED".equals(request.getStatus()))
                    update.set("closed_at", ts);
                mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
            } catch (Exception e) {
                log.warn("Bulk status update failed for id {}: {}", id, e.getMessage());
            }
        }

        return issueTicketRepository.findAllById(request.getIds());
    }

    @Override
    public List<IssueTicketEntity> bulkAssign(BulkAssignRequest request) {
        if (request.getIds() == null || request.getIds().isEmpty())
            return List.of();

        String now = TIMESTAMP_FMT.format(Instant.now());
        String assigneeLabel = request.getAssigneeName() != null ? request.getAssigneeName() : request.getAssignee();
        Comment bot = botComment(request.getAssignedBy(), "Assigned to " + assigneeLabel + ".");

        for (String id : request.getIds()) {
            try {
                Query query = new Query(Criteria.where("_id").is(id));
                Update update = new Update().set("assignee", request.getAssignee())
                        .set("assignee_name", request.getAssigneeName()).set("last_updated", now).push("comments", bot);
                mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
            } catch (Exception e) {
                log.warn("Bulk assign failed for id {}: {}", id, e.getMessage());
            }
        }

        return issueTicketRepository.findAllById(request.getIds());
    }

    // ─── Delete (soft) ───────────────────────────────────────────────────────

    @Override
    public IssueTicketEntity deleteTicket(String id, String deletedBy) {
        IssueTicketEntity existing = issueTicketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));

        String now = TIMESTAMP_FMT.format(Instant.now());
        StatusChangeLog deletedLog = new StatusChangeLog("DELETED", deletedBy, System.currentTimeMillis());

        Query query = new Query(Criteria.where("_id").is(existing.getId()));
        Update update = new Update().set("active_status", false).set("last_updated", now).push("status_history",
                deletedLog);

        mongoTemplate.updateFirst(query, update, IssueTicketEntity.class);
        log.info("Soft-deleted ticket {} by {}", existing.getTicketId(), deletedBy);
        return issueTicketRepository.findById(id).orElse(existing);
    }

    // ─── Skip Messages ────────────────────────────────────────────────────────

    @Override
    public List<IssueSkipMsgEntity> getSkipMessages() {
        return issueSkipMsgRepository.findAll();
    }

    @Override
    public IssueTicketEntity convertSkipMsgToTicket(String skipMsgId, String convertedBy) {
        IssueSkipMsgEntity skipMsg = issueSkipMsgRepository.findById(skipMsgId)
                .orElseThrow(() -> new IllegalArgumentException("Skip message not found: " + skipMsgId));

        if (Boolean.TRUE.equals(skipMsg.getConverted())) {
            throw new IllegalStateException(
                    "Skip message already converted to ticket: " + skipMsg.getConvertedTicketId());
        }

        String ticketId = generateNextTicketId();
        String now = TIMESTAMP_FMT.format(Instant.now());
        long ts = System.currentTimeMillis();

        StatusChangeLog openLog = new StatusChangeLog("OPEN", convertedBy, ts);

        IssueTicketEntity entity = IssueTicketEntity.builder().ticketId(ticketId).message(skipMsg.getMessage())
                .groupName(skipMsg.getGroupName()).senderName(skipMsg.getSenderName())
                .sender(skipMsg.getSender() != null ? skipMsg.getSender() : "").deviceImei("0")
                .divisionId(skipMsg.getDivisionId()).noteId(skipMsg.getNoteId())
                .sourceMessageId(skipMsg.getSourceMsgId())
                .postTime(skipMsg.getPostTime() != null ? skipMsg.getPostTime() : ts).priority("MEDIUM")
                .classifiedBy("manual").createdBy(convertedBy).affectedDevices(new ArrayList<>())
                .tags(new ArrayList<>()).attachments(new ArrayList<>()).watchers(new ArrayList<>())
                .watcherNames(new ArrayList<>()).linkedTickets(new ArrayList<>()).comments(new ArrayList<>())
                .commentAuditLog(new ArrayList<>()).transferHistory(new ArrayList<>()).isIssue(true).issueStatus("OPEN")
                .activeStatus(true).createdAt(now).statusHistory(new ArrayList<>(List.of(openLog))).build();

        IssueTicketEntity saved = issueTicketRepository.save(entity);
        log.info("Converted skip message {} to ticket {}", skipMsgId, saved.getTicketId());

        Query updateQuery = new Query(Criteria.where("_id").is(new org.bson.types.ObjectId(skipMsgId)));
        Update update = new Update().set("converted", true).set("converted_ticket_id", saved.getTicketId())
                .set("converted_by", convertedBy).set("converted_at", ts);
        mongoTemplate.updateFirst(updateQuery, update, IssueSkipMsgEntity.class);

        return saved;
    }

    // ─── Internal ────────────────────────────────────────────────────────────

    private Comment botComment(String triggeredBy, String text) {
        return Comment.create(text, triggeredBy != null ? triggeredBy : "system-bot", "SYSTEM", null);
    }

    private String generateNextTicketId() {
        return issueTicketRepository.findTopByOrderByTicketIdDesc().map(last -> {
            try {
                int num = Integer.parseInt(last.getTicketId().replace("TKT-", ""));
                return String.format("TKT-%05d", num + 1);
            } catch (Exception e) {
                return "TKT-00001";
            }
        }).orElse("TKT-00001");
    }
}
