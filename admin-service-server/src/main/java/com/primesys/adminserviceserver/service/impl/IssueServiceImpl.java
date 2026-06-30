package com.primesys.adminserviceserver.service.impl;

import com.mongodb.Function;
import com.mongodb.MongoBulkWriteException;
import com.primesys.adminservicecommon.dto.*;
import com.primesys.adminservicemongodb.entity.*;
import com.primesys.adminservicemongodb.entity.IssueSkipMsgEntity;
import com.primesys.adminservicemongodb.model.GroupCount;
import com.primesys.adminservicemongodb.model.UpdateAuditLog;
import com.primesys.adminservicemongodb.model.UpdateIssueRequest;
import com.primesys.adminservicemongodb.repository.IssueCategoryRepository;
import com.primesys.adminservicemongodb.repository.IssueDataRepository;
import com.primesys.adminservicemongodb.repository.IssueSkipMsgRepository;
import com.primesys.adminservicemongodb.repository.IssueTransferAuditRepository;
import com.primesys.adminservicemongodb.repository.WMessageRepository;
import com.primesys.adminserviceserver.exceptionHandler.exceptions.ResourceNotFoundException;
import com.primesys.adminserviceserver.request.AddCommentRequest;
import com.primesys.adminserviceserver.request.DeleteCommentRequest;
import com.primesys.adminserviceserver.request.EditCommentRequest;
import com.primesys.adminserviceserver.request.MessageRequest;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.response.IssueResponseDto;
import com.primesys.adminserviceserver.service.IssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final WMessageRepository wMessageRepository;
    private final MongoTemplate mongoTemplate;

    private final IssueDataRepository issueDataRepository;
    private final IssueSkipMsgRepository issueSkipMsgRepository;
    @Autowired
    private IssueTransferAuditRepository auditRepository;
    private final DivisionLoginServiceImpl divisionLoginService;
    private final IssueCategoryRepository issueCategoryRepository;

    @Override
    public Optional<List<WMessageEntity>> saveMsg(List<MessageRequest> msgList) {
        if (msgList == null || msgList.isEmpty()) {
            return Optional.empty();
        }

        Set<String> inputNoteIds = msgList.stream().map(MessageRequest::getNoteId).filter(Objects::nonNull)
                .map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());

        // Query existing noteIds from DB to avoid known duplicates
        Query query = new Query(Criteria.where("noteId").in(inputNoteIds));
        List<WMessageEntity> existingEntities = mongoTemplate.find(query, WMessageEntity.class);
        Set<String> existingNoteIds = existingEntities.stream().map(e -> e.getNoteId().trim().toLowerCase())
                .collect(Collectors.toSet());

        // Filter messages that are not from primesys and not already existing
        List<WMessageEntity> entitiesToSave = msgList.stream()
                .filter(e -> !e.getSender().toLowerCase().contains("primesys"))
                .filter(e -> e.getNoteId() != null && !existingNoteIds.contains(e.getNoteId().trim().toLowerCase()))
                .map(msg -> {
                    String senderRaw = msg.getSender();
                    String[] parts = senderRaw.split(":");

                    String groupName = parts[0].trim().replaceAll("\\(\\d+ messages\\)", "").trim();
                    String senderName = parts.length > 1 ? parts[1].replace("~", "").replace(" ", "").trim() : "";

                    Optional<DivisionLoginEntity> optionalDivision = divisionLoginService
                            .getDivisionFromWGroupName(groupName);
                    String divisionId = optionalDivision.map(DivisionLoginEntity::getId).orElse("No division match");

                    return WMessageEntity.builder().message(msg.getMessage()).postTime(msg.getPostTime())
                            .groupName(groupName).senderName(senderName).sender(senderRaw).divisionId(divisionId)
                            .isIssue(false).noteId(msg.getNoteId()).activeStatus(true).build();
                }).collect(Collectors.toList());

        try {
            List<WMessageEntity> savedEntities = (List<WMessageEntity>) mongoTemplate.insertAll(entitiesToSave);
            // Combine saved and existing records
            List<WMessageEntity> allRecords = new ArrayList<>(existingEntities);
            allRecords.addAll(savedEntities);
            return Optional.of(allRecords);

        } catch (MongoBulkWriteException e) {
            // Extract duplicate noteIds from exception
            List<String> duplicateNoteIds = e.getWriteErrors().stream()
                    .map(err -> err.getMessage().replaceAll(".*dup key: \\{ note_id: \"(.*?)\" \\}.*", "$1"))
                    .collect(Collectors.toList());

            // Fetch those duplicate records from DB
            Query dupQuery = new Query(Criteria.where("noteId").in(duplicateNoteIds));
            List<WMessageEntity> duplicateRecords = mongoTemplate.find(dupQuery, WMessageEntity.class);

            // Combine all existing + duplicate records
            List<WMessageEntity> allRecords = new ArrayList<>(existingEntities);
            allRecords.addAll(duplicateRecords);

            return Optional.of(allRecords);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<WMessageEntity>> getWhatsAppMsg() {
        long oneWeekAgoMillis = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000);
        return Optional.of(wMessageRepository.findByActiveStatusTrueAndPostTimeGreaterThan(oneWeekAgoMillis));
    }

    @Override
    public boolean isIssuePickable(String noteId) {
        List<WMessageEntity> wMessageEntities = wMessageRepository.findAllByNoteId(noteId);
        if (wMessageEntities.isEmpty()) {
            throw new ResourceNotFoundException("Issue not found");
        } else if (wMessageEntities.size() > 1) {
            for (int i = 0; i < wMessageEntities.size() - 1; i++) {
                wMessageRepository.deleteById(wMessageEntities.get(i + 1).getId());
            }
        }
        return wMessageEntities.get(0).getIsIssue() == true;
    }

    @Override
    public String pickUpIssue(String noteId, String userId, String action) {
        List<WMessageEntity> wMessageEntities = wMessageRepository.findAllByNoteId(noteId);

        if (wMessageEntities.isEmpty()) {
            throw new IllegalArgumentException("No Issue found with this IssueID");
        }

        if (wMessageEntities.size() > 1) {
            for (int i = 0; i < wMessageEntities.size() - 1; i++) {
                wMessageRepository.deleteById(wMessageEntities.get(i + 1).getId());
            }
        }

        WMessageEntity wMessageEntity = wMessageEntities.get(0);

        if (wMessageEntity.getIsIssue() == true)
            return "Issue has been already picked by "
                    + divisionLoginService.getDivisionFromId(wMessageEntity.getUpdatedBy()) + ".";

        long now = System.currentTimeMillis();
        wMessageEntity.setUpdatedBy(userId);
        wMessageEntity.setActionBy(userId);
        wMessageEntity.setUpdatedAt(now / 1000);

        wMessageRepository.save(wMessageEntity);

        boolean alreadySkipped = issueSkipMsgRepository.findByNoteId(wMessageEntity.getNoteId()).isPresent();
        if (!alreadySkipped) {
            IssueSkipMsgEntity skipMsg = IssueSkipMsgEntity.builder().noteId(wMessageEntity.getNoteId())
                    .message(wMessageEntity.getMessage()).sender(wMessageEntity.getSender())
                    .senderName(wMessageEntity.getSenderName()).groupName(wMessageEntity.getGroupName())
                    .divisionId(wMessageEntity.getDivisionId()).postTime(wMessageEntity.getPostTime())
                    .sourceMsgId(wMessageEntity.getId()).skippedBy(userId)
                    .skippedAt(java.time.Instant.ofEpochMilli(now).toString()).converted(false).build();
            issueSkipMsgRepository.save(skipMsg);
        }

        return "Issue has been skipped successfully";
    }

    @Override
    public Optional<List<IssueResponseDto>> getIssuePickByMember(String userId) {
        List<IssueResponseDto> updatedList;

        if ("all".equalsIgnoreCase(userId)) {
            updatedList = issueDataRepository.findByActiveStatusTrueAndIssueStatusNot("CLOSE").stream().peek(issue -> {
                String assigneeId = issue.getAssignee();
                if (assigneeId != null) {
                    String assigneeName = divisionLoginService.getDivisionFromId(assigneeId);
                    issue.setAssigneeName(assigneeName);
                }

                String creatorId = issue.getCreatedBy();
                if (creatorId != null) {
                    String creatorName = divisionLoginService.getDivisionFromId(creatorId);
                    issue.setCreatedBy(creatorName);
                }
            }).map(issue -> {
                List<Comment> updatedComments = issue.getComments() != null ? issue.getComments().stream()
                        .map(comment -> Comment.builder().id(comment.getId()).message(comment.getMessage())
                                .commentedAt(comment.getCommentedAt())
                                .commentedBy(comment.getCommentedBy() != null
                                        ? divisionLoginService.getDivisionFromId(comment.getCommentedBy())
                                        : issue.getSenderName())
                                .build())
                        .toList() : null;

                return IssueResponseDto.builder().id(issue.getId()).wMsgId(issue.getWMsgId()).sender(issue.getSender())
                        .groupName(issue.getGroupName()).senderName(issue.getSenderName()).message(issue.getMessage())
                        .noteId(issue.getNoteId()).postTime(issue.getPostTime()).isIssue(issue.getIsIssue())
                        .issueStatus(issue.getIssueStatus()).priority(issue.getPriority()).category(issue.getCategory())
                        .assignee(issue.getAssignee()).assigneeName(issue.getAssigneeName())
                        .previousAssignee(issue.getPreviousAssignee()).transferHistory(issue.getTransferHistory())
                        .comments(updatedComments).tags(issue.getTags()).attachments(issue.getAttachments())
                        .dueDate(issue.getDueDate()).reopenCount(issue.getReopenCount())
                        .activeStatus(issue.getActiveStatus()).actionBy(issue.getActionBy())
                        .updatedBy(issue.getUpdatedBy()).updatedAt(issue.getUpdatedAt()).createdBy(issue.getCreatedBy())
                        .createdAt(issue.getCreatedAt()).divisionId(issue.getDivisionId())
                        .deviceImei(issue.getDeviceImei()).build();
            }).toList();

        } else {
            updatedList = issueDataRepository.findByActiveStatusTrueAndCreatedBy(userId).stream().peek(issue -> {
                String assigneeId = issue.getAssignee();
                if (assigneeId != null) {
                    String assigneeName = divisionLoginService.getDivisionFromId(assigneeId);
                    issue.setAssigneeName(assigneeName);
                }

                String creatorId = issue.getCreatedBy();
                if (creatorId != null) {
                    String creatorName = divisionLoginService.getDivisionFromId(creatorId);
                    issue.setCreatedBy(creatorName);
                }
            }).map(issue -> IssueResponseDto.builder().id(issue.getId()).wMsgId(issue.getWMsgId())
                    .sender(issue.getSender()).groupName(issue.getGroupName()).senderName(issue.getSenderName())
                    .message(issue.getMessage()).noteId(issue.getNoteId()).postTime(issue.getPostTime())
                    .isIssue(issue.getIsIssue()).issueStatus(issue.getIssueStatus()).priority(issue.getPriority())
                    .category(issue.getCategory()).assignee(issue.getAssignee()).assigneeName(issue.getAssigneeName())
                    .previousAssignee(issue.getPreviousAssignee()).transferHistory(issue.getTransferHistory())
                    .comments(issue.getComments()).tags(issue.getTags()).attachments(issue.getAttachments())
                    .dueDate(issue.getDueDate()).reopenCount(issue.getReopenCount())
                    // .statusHistory(issue.getStatusHistory())
                    .activeStatus(issue.getActiveStatus()).actionBy(issue.getActionBy()).updatedBy(issue.getUpdatedBy())
                    .updatedAt(issue.getUpdatedAt()).createdBy(issue.getCreatedBy()).createdAt(issue.getCreatedAt())
                    .divisionId(issue.getDivisionId())
                    // .commentAuditLogs(issue.getCommentAuditLogs())
                    // .updateAuditLogs(issue.getUpdateAuditLogs())
                    .deviceImei(issue.getDeviceImei()).build()).toList();
        }

        return Optional.of(updatedList);
    }

    public IssueEntity transferIssue(String issueId, String newAssignee, String transferredBy, String reason) {
        IssueEntity issue = issueDataRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        String previousAssignee = issue.getAssignee();

        // Update issue fields
        issue.setPreviousAssignee(previousAssignee);
        issue.setAssignee(newAssignee);

        // Update transfer history in the issue itself
        TransferLog log = TransferLog.builder().from(previousAssignee).to(newAssignee).transferredBy(transferredBy)
                .transferredAt(System.currentTimeMillis()).build();
        if (issue.getTransferHistory() == null) {
            issue.setTransferHistory(new ArrayList<>());
        }
        issue.getTransferHistory().add(log);
        issueDataRepository.save(issue);

        // Save audit log
        IssueTransferAudit audit = IssueTransferAudit.builder().issueId(issueId).fromAssignee(previousAssignee)
                .toAssignee(newAssignee).transferredBy(transferredBy).transferredAt(System.currentTimeMillis())
                .transferReason(reason).build();

        auditRepository.save(audit);
        return issue;
    }

    public void updateIssueStatus(String issueId, String newStatus, String updatedBy) {
        IssueEntity issue = issueDataRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        String currentStatus = issue.getIssueStatus();
        if (!newStatus.equalsIgnoreCase(currentStatus)) {
            issue.setIssueStatus(newStatus);

            // Update status history
            StatusChangeLog log = new StatusChangeLog(newStatus, updatedBy, System.currentTimeMillis());
            if (issue.getStatusHistory() == null) {
                issue.setStatusHistory(new ArrayList<>());
            }
            issue.getStatusHistory().add(log);
        }

        issue.setUpdatedAt(System.currentTimeMillis());
        issue.setUpdatedBy(updatedBy);
        issueDataRepository.save(issue);
    }

    // public IssueEntity createIssue(CreateIssueRequest request) {
    // IssueEntity issue = IssueEntity.builder().wMsgId(request.getWMsgId()).sender(request.getSender())
    // .groupName(request.getGroupName()).senderName(request.getSenderName()).message(request.getMessage())
    // .noteId(request.getNoteId()).postTime(request.getPostTime()).isIssue(request.getIsIssue())
    // .issueStatus(request.getIssueStatus()).priority(request.getPriority()).category(request.getCategory())
    // .assignee(request.getAssignee()).comments(request.getComments()).tags(request.getTags())
    // .attachments(request.getAttachments()).dueDate(request.getDueDate()).createdBy(request.getCreatedBy())
    // .createdAt(System.currentTimeMillis()).activeStatus(true)
    // .statusHistory(List.of(new StatusChangeLog(request.getIssueStatus(),
    //
    // request.getCreatedBy(), System.currentTimeMillis())))
    // .build();
    //
    // return issueDataRepository.save(issue);
    // }

    public IssueEntity createIssue(CreateIssueRequest request) {
        log.info("createIssue  {}", request);
        List<StatusChangeLog> statusHistory = new ArrayList<>();
        if (request.getIssueStatus() != null) {
            statusHistory.add(
                    new StatusChangeLog(request.getIssueStatus(), request.getCreatedBy(), System.currentTimeMillis()));
        }

        IssueEntity issue = IssueEntity.builder().wMsgId(request.getWMsgId()).sender(request.getSender())
                .groupName(request.getGroupName()).senderName(request.getSenderName()).message(request.getMessage())
                .noteId(request.getNoteId()).postTime(request.getPostTime()).isIssue(request.getIsIssue())
                .issueStatus(request.getIssueStatus()).priority(request.getPriority()).category(request.getCategory())
                .assignee(request.getAssignee()).divisionId(
                        request.getDivisionId())
                .assigneeName(divisionLoginService.getDivisionFromId(request.getAssignee()))
                .comments(request.getComments() != null ? request.getComments().stream()
                        .map(comment -> Comment.builder()
                                .id(comment.getId() != null ? comment.getId() : UUID.randomUUID().toString())
                                .message(comment.getMessage() != null ? comment.getMessage() : "Ticket Initiated")
                                .commentedBy(comment.getCommentedBy() != null
                                        ? divisionLoginService.getDivisionFromId(comment.getCommentedBy())
                                        : request.getSenderName()) // fallback
                                .commentedAt(comment.getCommentedAt() != null && comment.getCommentedAt() > 0
                                        ? comment.getCommentedAt() : System.currentTimeMillis())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .tags(request.getTags())
                .attachments(request.getAttachments() != null && !request.getAttachments().isEmpty()
                        ? request.getAttachments() : new ArrayList<>())
                .dueDate(request.getDueDate()).createdBy(request.getCreatedBy()).createdAt(System.currentTimeMillis())
                .activeStatus(true).deviceImei(request.getDeviceImei()).statusHistory(statusHistory).build();

        // Save the issue
        IssueEntity savedIssue = issueDataRepository.save(issue);

        // If save succeeded, update WMessageEntity
        if (savedIssue != null && savedIssue.getId() != null) {
            Optional<WMessageEntity> wMessageEntityOptional = wMessageRepository.findByNoteId(request.getNoteId());
            if (wMessageEntityOptional.isPresent()) {
                WMessageEntity wMessageEntity = wMessageEntityOptional.get();
                long currentTimestamp = System.currentTimeMillis() / 1000;

                wMessageEntity.setUpdatedBy(request.getCreatedBy());
                wMessageEntity.setActionBy(request.getCreatedBy());
                wMessageEntity.setUpdatedAt(currentTimestamp);
                wMessageEntity.setActiveStatus(false);
                wMessageEntity.setIsIssue(true);

                wMessageRepository.save(wMessageEntity);
            }
        }

        return savedIssue;
    }

    @Override
    public IssueEntity updateIssue(UpdateIssueRequest request) {
        log.info("updateIssue  {}", request);

        IssueEntity existing = issueDataRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        IssueEntity incoming = request.getIssue();
        String updatedBy = request.getUpdatedBy();
        long now = System.currentTimeMillis();
        log.info("updateIssue existing  {}", existing);

        List<UpdateAuditLog> changes = new ArrayList<>();

        // Immutable fields
        incoming.setId(new ObjectId(existing.getId()));
        incoming.setWMsgId(existing.getWMsgId());
        incoming.setCreatedAt(existing.getCreatedAt());
        incoming.setCreatedBy(existing.getCreatedBy());

        // Compare each field (excluding immutable fields)
        compareField("sender", existing.getSender(), incoming.getSender(), changes, updatedBy, now);
        compareField("groupName", existing.getGroupName(), incoming.getGroupName(), changes, updatedBy, now);
        compareField("senderName", existing.getSenderName(), incoming.getSenderName(), changes, updatedBy, now);
        compareField("message", existing.getMessage(), incoming.getMessage(), changes, updatedBy, now);
        compareField("noteId", existing.getNoteId(), incoming.getNoteId(), changes, updatedBy, now);
        compareField("postTime", existing.getPostTime(), incoming.getPostTime(), changes, updatedBy, now);
        compareField("isIssue", existing.getIsIssue(), incoming.getIsIssue(), changes, updatedBy, now);
        compareField("issueStatus", existing.getIssueStatus(), incoming.getIssueStatus(), changes, updatedBy, now);
        compareField("priority", existing.getPriority(), incoming.getPriority(), changes, updatedBy, now);
        compareField("category", existing.getCategory(), incoming.getCategory(), changes, updatedBy, now);
        compareField("assignee", existing.getAssignee(), incoming.getAssignee(), changes, updatedBy, now);
        compareField("previousAssignee", existing.getPreviousAssignee(), incoming.getPreviousAssignee(), changes,
                updatedBy, now);
        compareField("dueDate", existing.getDueDate(), incoming.getDueDate(), changes, updatedBy, now);
        compareField("reopenCount", existing.getReopenCount(), incoming.getReopenCount(), changes, updatedBy, now);
        compareField("activeStatus", existing.getActiveStatus(), incoming.getActiveStatus(), changes, updatedBy, now);
        compareField("actionBy", existing.getActionBy(), incoming.getActionBy(), changes, updatedBy, now);
        compareField("updatedBy", existing.getUpdatedBy(), incoming.getUpdatedBy(), changes, updatedBy, now);
        compareField("divisionId", existing.getDivisionId(), incoming.getDivisionId(), changes, updatedBy, now);

        // Optional: serialize list fields as comma-separated for comparison
        compareField("tags", existing.getTags(), incoming.getTags(), changes, updatedBy, now);
        // compareField("attachments", existing.getAttachments(), incoming.getAttachments(), changes, updatedBy, now);
        compareField("deviceImei", existing.getDeviceImei(), incoming.getDeviceImei(), changes, updatedBy, now);

        // Update all mutable fields
        existing.setSender(incoming.getSender());
        existing.setGroupName(incoming.getGroupName());
        existing.setSenderName(incoming.getSenderName());
        existing.setMessage(incoming.getMessage());
        existing.setNoteId(incoming.getNoteId());
        existing.setPostTime(incoming.getPostTime());
        existing.setIsIssue(incoming.getIsIssue());
        existing.setIssueStatus(incoming.getIssueStatus());
        existing.setPriority(incoming.getPriority());
        existing.setCategory(incoming.getCategory());
        existing.setAssigneeName(divisionLoginService.getDivisionFromId(incoming.getAssignee()));
        existing.setAssignee(incoming.getAssignee());
        existing.setPreviousAssignee(incoming.getPreviousAssignee());
        existing.setDueDate(incoming.getDueDate());
        existing.setReopenCount(incoming.getReopenCount());
        existing.setActiveStatus(incoming.getActiveStatus());
        existing.setActionBy(incoming.getActionBy());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedAt(now);
        existing.setTags(incoming.getTags());
        existing.setAttachments(incoming.getAttachments());
        // existing.setComments(incoming.getComments());
        existing.setTransferHistory(incoming.getTransferHistory());
        existing.setStatusHistory(incoming.getStatusHistory());
        existing.setDeviceImei(incoming.getDeviceImei());

        // Save audit logs
        if (!changes.isEmpty()) {
            if (existing.getUpdateAuditLogs() == null) {
                existing.setUpdateAuditLogs(new ArrayList<>());
            }
            existing.getUpdateAuditLogs().addAll(changes);
        }

        return issueDataRepository.save(existing);
    }

    // public IssueEntity updateIssue(UpdateIssueRequest request) {
    // IssueEntity existing = issueDataRepository.findById(request.getIssueId())
    // .orElseThrow(() -> new IllegalArgumentException("Issue not found with ID: " + request.getIssueId()));
    //
    // IssueEntity incoming = request.getIssue();
    // String updatedBy = request.getUpdatedBy();
    // long now = System.currentTimeMillis();
    //
    // // === IMMUTABLE FIELDS: enforce original values ===
    // incoming.setId(new ObjectId(existing.getId()));
    // incoming.setWMsgId(existing.getWMsgId());
    // incoming.setCreatedBy(existing.getCreatedBy());
    // incoming.setCreatedAt(existing.getCreatedAt());
    //
    // // === Set update metadata ===
    // incoming.setUpdatedBy(updatedBy);
    // incoming.setUpdatedAt(now);
    //
    // // === Detect field changes ===
    // List<UpdateAuditLog> changes = new ArrayList<>();
    // compareField("priority", existing.getPriority(), incoming.getPriority(), changes, updatedBy, now);
    // compareField("assignee", existing.getAssignee(), incoming.getAssignee(), changes, updatedBy, now);
    // compareField("issueStatus", existing.getIssueStatus(), incoming.getIssueStatus(), changes, updatedBy, now);
    // compareField("category", existing.getCategory(), incoming.getCategory(), changes, updatedBy, now);
    // compareField("message", existing.getMessage(), incoming.getMessage(), changes, updatedBy, now);
    // // Add more fields as needed
    //
    // if (!changes.isEmpty()) {
    // if (existing.getUpdateAuditLogs() == null) {
    // existing.setUpdateAuditLogs(new ArrayList<>());
    // }
    // existing.getUpdateAuditLogs().addAll(changes);
    // }
    //
    // // === Copy allowed fields from incoming ===
    // existing.setPriority(incoming.getPriority());
    // existing.setIssueStatus(incoming.getIssueStatus());
    // existing.setCategory(incoming.getCategory());
    // existing.setAssignee(incoming.getAssignee());
    // existing.setGroupName(incoming.getGroupName());
    // existing.setSenderName(incoming.getSenderName());
    // existing.setMessage(incoming.getMessage());
    // existing.setDueDate(incoming.getDueDate());
    // existing.setComments(incoming.getComments());
    // existing.setTags(incoming.getTags());
    // existing.setAttachments(incoming.getAttachments());
    // existing.setIsIssue(incoming.getIsIssue());
    // existing.setDivisionId(incoming.getDivisionId());
    // existing.setUpdatedAt(now);
    // existing.setUpdatedBy(updatedBy);
    //
    // return issueDataRepository.save(existing);
    // }

    @Override
    public Optional<FileUploadResultResponse> uploadIssueAttachmentFile(String col, String s, String updatedBy) {
        return Optional.empty();
    }

    @Override
    public IssueEntity addComment(AddCommentRequest request) {
        IssueEntity issue = issueDataRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        Comment comment = Comment.create(request.getMessage(), request.getCommentedBy());

        if (issue.getComments() == null) {
            issue.setComments(new ArrayList<>());
        }

        issue.getComments().add(comment);
        issue.setUpdatedAt(System.currentTimeMillis());

        return issueDataRepository.save(issue);
    }

    @Override
    public IssueEntity editComment(EditCommentRequest request) {
        IssueEntity issue = issueDataRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        Comment comment = issue.getComments().stream().filter(c -> c.getId().equals(request.getCommentId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        String oldMessage = comment.getMessage();

        comment.setMessage(request.getUpdatedMessage());
        comment.setCommentedAt(System.currentTimeMillis());

        CommentAuditLog log = new CommentAuditLog(comment.getId(), "EDIT", request.getUpdatedBy(),
                System.currentTimeMillis(), oldMessage, request.getUpdatedMessage());

        if (issue.getCommentAuditLogs() == null) {
            issue.setCommentAuditLogs(new ArrayList<>());
        }
        issue.getCommentAuditLogs().add(log);
        issue.setUpdatedAt(System.currentTimeMillis());

        return issueDataRepository.save(issue);
    }

    @Override

    public IssueEntity deleteComment(DeleteCommentRequest request) {
        IssueEntity issue = issueDataRepository.findById(request.getIssueId())
                .orElseThrow(() -> new IllegalArgumentException("Issue not found"));

        Comment comment = issue.getComments().stream().filter(c -> c.getId().equals(request.getCommentId())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        issue.getComments().remove(comment);

        CommentAuditLog log = new CommentAuditLog(comment.getId(), "DELETE", request.getDeletedBy(),
                System.currentTimeMillis(), comment.getMessage(), null);

        if (issue.getCommentAuditLogs() == null) {
            issue.setCommentAuditLogs(new ArrayList<>());
        }
        issue.getCommentAuditLogs().add(log);
        issue.setUpdatedAt(System.currentTimeMillis());

        return issueDataRepository.save(issue);
    }

    @Override
    public List<GroupCount> getGroupedSummary(List<String> groupFields) {
        if (groupFields == null || groupFields.isEmpty()) {
            throw new IllegalArgumentException("At least one group field is required");
        }

        // Step 1: Convert group fields to array
        String[] groupByFields = groupFields.toArray(new String[0]);
        boolean singleFieldGroup = groupFields.size() == 1;

        // Step 2: Optional - Filter out documents where group fields are null
        Criteria criteria = new Criteria();
        List<Criteria> nonNullCriteria = groupFields.stream().map(field -> Criteria.where(field).ne(null)).toList();
        if (!nonNullCriteria.isEmpty()) {
            criteria = new Criteria().andOperator(nonNullCriteria.toArray(new Criteria[0]));
        }

        MatchOperation matchOp = Aggregation.match(criteria);

        // Step 3: Group by fields
        GroupOperation groupOp = Aggregation.group(groupByFields).count().as("count");

        // Step 4: Project fields properly (handle single field group differently)
        ProjectionOperation projectOp;
        if (singleFieldGroup) {
            String field = groupFields.get(0);
            projectOp = Aggregation.project("count").and("_id").as(field);
        } else {
            projectOp = Aggregation.project("count");
            for (String field : groupFields) {
                projectOp = projectOp.and("_id." + field).as(field);
            }
        }

        // Step 5: Build aggregation
        Aggregation aggregation = Aggregation.newAggregation(matchOp, groupOp, projectOp);

        // Step 6: Run aggregation
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "issue_data", Document.class);

        return results.getMappedResults().stream().map(doc -> {
            Map<String, String> groupMap = new HashMap<>();

            for (String field : groupFields) {
                Object val = doc.get(field);
                groupMap.put(field, val != null ? val.toString() : "null");
            }

            // ✅ Add division name before creating GroupCount
            if (groupMap.containsKey("divisionId")) {
                String divisionId = groupMap.get("divisionId");
                String divisionName = divisionLoginService.getDivisionFromId(divisionId);
                groupMap.put("divisionName", divisionName);
            }

            Number countVal = doc.get("count", Number.class);
            long count = countVal != null ? countVal.longValue() : 0L;

            return new GroupCount(groupMap, count);
        }).toList();

    }

    // @Override
    // public IssueAnalyticsDTO getIssueAnalytics(String assigneeId, String status, String priority, String category,
    // Long startDate, Long endDate) {
    // long startOfToday = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toEpochSecond();
    //
    // // 1. Status count
    // Aggregation statusAgg = Aggregation.newAggregation(Aggregation.group("issue_status").count().as("count"));
    //
    // // 2. Assignee + Status grouping
    // Aggregation assigneeStatusAgg = Aggregation
    // .newAggregation(Aggregation.group("assignee", "issue_status").count().as("count"));
    //
    // // 3. Today's status
    // Aggregation todayStatusAgg = Aggregation.newAggregation(
    // Aggregation.match(Criteria.where("created_at").gte(startOfToday)),
    // Aggregation.group("issue_status").count().as("count"));
    //
    // // 4. Priority vs Status
    // Aggregation priorityStatusAgg = Aggregation
    // .newAggregation(Aggregation.group("priority", "issue_status").count().as("count"));
    //
    // // 5. Reopened count
    // Aggregation reopenedAgg = Aggregation.newAggregation(Aggregation.match(Criteria.where("reopen_count").gt(0)),
    // Aggregation.count().as("reopened"));
    //
    // // 6. SLA breached
    // Aggregation slaAgg = Aggregation.newAggregation(Aggregation
    // .match(new Criteria().andOperator(Criteria.where("due_date").lt(System.currentTimeMillis() / 1000),
    // Criteria.where("issue_status").ne("CLOSED"))),
    // Aggregation.count().as("slaBreached"));
    //
    // // 7. Tag usage
    // Aggregation tagsAgg = Aggregation.newAggregation(Aggregation.unwind("tags"),
    // Aggregation.group("tags").count().as("count"), Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
    // Aggregation.limit(10));
    //
    // // 8. Latest issues
    // Aggregation latestAgg = Aggregation.newAggregation(Aggregation.sort(Sort.by(Sort.Direction.DESC, "created_at")),
    // Aggregation.limit(100)
    // // Aggregation.project("assignee", "issue_status",
    // // "created_at").and(ConvertOperators.ToString.toString("_id")).as("id")
    // );
    //
    // // Execute
    // Map<String, Long> statusCounts = toMap(mongoTemplate.aggregate(statusAgg, "issue_data", Document.class));
    // Map<String, Map<String, Long>> assigneeStatus = toNestedMap(
    // mongoTemplate.aggregate(assigneeStatusAgg, "issue_data", Document.class));
    // Map<String, Long> todayCounts = toMap(mongoTemplate.aggregate(todayStatusAgg, "issue_data", Document.class));
    // List<PriorityStatusCountDTO> priorityStatus = toPriorityStatus(
    // mongoTemplate.aggregate(priorityStatusAgg, "issue_data", Document.class));
    // long reopened = getCountFromSingle(mongoTemplate.aggregate(reopenedAgg, "issue_data", Document.class),
    // "reopened");
    // long sla = getCountFromSingle(mongoTemplate.aggregate(slaAgg, "issue_data", Document.class), "slaBreached");
    // List<TagUsageDTO> tags = toTagList(mongoTemplate.aggregate(tagsAgg, "issue_data", Document.class));
    // List<IssueEntity> latest = mongoTemplate.aggregate(latestAgg, "issue_data", IssueEntity.class)
    // .getMappedResults();
    //
    // // return new IssueAnalyticsDTO(statusCounts, assigneeStatus, todayCounts, priorityStatus, reopened, sla, tags,
    // // latest);
    // return IssueAnalyticsDTO.builder().todayStatusCountsGlobal(statusCounts).assigneeStatusCounts(assigneeStatus)
    // .todayStatusCounts(todayCounts).pagedLatestIssues(latest).priorityStatusCounts(priorityStatus)
    // .reopenedIssuesCount(reopened).slaBreachedCount(sla).build();
    // }

    // @Override
    // public IssueAnalyticsDTO getIssueAnalyticsFiltered(String assigneeId, String status, String priority,
    // String category, Long startDate, Long endDate, int page, int size) {
    //
    // List<Criteria> criteriaList = new ArrayList<>();
    //
    // if (StringUtils.hasText(assigneeId)) {
    // criteriaList.add(Criteria.where("assignee").is(assigneeId));
    // }
    // if (StringUtils.hasText(status)) {
    // criteriaList.add(Criteria.where("issue_status").is(status));
    // }
    // if (StringUtils.hasText(priority)) {
    // criteriaList.add(Criteria.where("priority").is(priority));
    // }
    // if (StringUtils.hasText(category)) {
    // criteriaList.add(Criteria.where("category").is(category));
    // }
    // if (startDate != null || endDate != null) {
    // Criteria dateCriteria = Criteria.where("created_at");
    // if (startDate != null)
    // dateCriteria.gte(startDate);
    // if (endDate != null)
    // dateCriteria.lte(endDate);
    // criteriaList.add(dateCriteria);
    // }
    //
    // Criteria matchCriteria = criteriaList.isEmpty() ? new Criteria()
    // : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
    // MatchOperation match = Aggregation.match(matchCriteria);
    //
    // long todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
    // MatchOperation matchToday = Aggregation.match(Criteria.where("created_at").gte(todayStart));
    //
    // // Define $facet aggregation
    // FacetOperation facetOp = Aggregation.facet(Aggregation.group("issue_status").count().as("count"))
    // .as("totalStatusCounts")
    // .and(Aggregation.group("assignee", "issue_status").count().as("count"),
    // Aggregation.project("count").and("_id.assignee").as("assignee").and("_id.issue_status")
    // .as("status"))
    // .as("assigneeStatusCounts").and(matchToday, Aggregation.group("issue_status").count().as("count"))
    // .as("todayStatusCounts")
    // .and(Aggregation.sort(Sort.by(Sort.Direction.DESC, "created_at")), Aggregation.limit(100))
    // .as("latestIssues")
    // // ➕ Overdue issues
    // .and(Aggregation.match(Criteria.where("due_date").lt(System.currentTimeMillis() / 1000)
    // .and("issue_status").ne("CLOSED")), Aggregation.count().as("count"))
    // .as("overdueIssues")
    //
    // // ➕ Avg time to close
    // .and(Aggregation.match(Criteria.where("issue_status").is("CLOSED").and("created_at").ne(null)
    // .and("updated_at").ne(null)),
    // Aggregation.project()
    // .and(ArithmeticOperators.Subtract.valueOf("updated_at").subtract("created_at"))
    // .as("duration"),
    // Aggregation.group().avg("duration").as("avgDuration"))
    // .as("avgTimeToClose")
    //
    // // ➕ Trend per day
    // // .and(
    // // Aggregation.project("issue_status", "created_at")
    // // .and(
    // // DateOperators.DateToString
    // // .dateOf(ArithmeticOperators.Multiply.valueOf("created_at").multiplyBy(1000))
    // // .toString("%Y-%m-%d")
    // // ).as("day")
    // // .and("issue_status").as("status"),
    // //
    // // Aggregation.group("day", "status").count().as("count"),
    // // Aggregation.sort(Sort.by("day").ascending())
    // // ).as("statusTrendPerDay")
    //
    // // Let’s assume an SLA means a ticket should be closed within 72 hours (configurable):
    // .and(Aggregation.match(Criteria.where("issue_status").is("CLOSED").and("created_at").ne(null)
    // .and("updated_at").ne(null)),
    // Aggregation.project()
    // .and(ArithmeticOperators.Subtract.valueOf("updated_at").subtract("created_at"))
    // .as("duration"),
    // Aggregation.match(Criteria.where("duration").gt(72 * 3600)), // 72 hours in seconds
    // Aggregation.count().as("count"))
    // .as("slaBreachedCount")
    // // ➕ Trend per week
    // // .and(
    // // Aggregation.project("issue_status", "created_at")
    // // .and(
    // // DateOperators.DateToString
    // // .dateOf(ArithmeticOperators.Multiply.valueOf("created_at").multiplyBy(1000))
    // // .toString("%Y-%U") // week format
    // // ).as("week")
    // // .and("issue_status").as("status"),
    // //
    // // Aggregation.group("week", "status").count().as("count"),
    // // Aggregation.sort(Sort.by("week").ascending())
    // // ).as("statusTrendPerWeek")
    //
    // // ➕ Trend per month
    // // .and(
    // // Aggregation.project("issue_status", "created_at")
    // // .and(
    // // DateOperators.DateToString
    // // .dateOf(ArithmeticOperators.Multiply.valueOf("created_at").multiplyBy(1000))
    // // .toString("%Y-%m") // Month format: e.g., "2025-07"
    // // ).as("month")
    // // .and("issue_status").as("status"),
    // //
    // // Aggregation.group("month", "status").count().as("count"),
    // // Aggregation.sort(Sort.by("month").ascending())
    // // ).as("statusTrendPerMonth");
    // ;
    //
    // Aggregation aggregation = Aggregation.newAggregation(match, facetOp);
    // AggregationResults<Document> aggResult = mongoTemplate.aggregate(aggregation, "issue_data", Document.class);
    // Document resultDoc = aggResult.getUniqueMappedResult();
    //
    // if (resultDoc == null)
    // return new IssueAnalyticsDTO(); // Return empty
    //
    // // Parse each result from facets
    // Map<String, Long> totalStatusCounts = parseSimpleCountMap((List<Document>) resultDoc.get("totalStatusCounts"));
    //
    // Map<String, Map<String, Long>> assigneeStatusCounts = new HashMap<>();
    // for (Document doc : (List<Document>) resultDoc.get("assigneeStatusCounts")) {
    // String assignee = Optional.ofNullable(doc.getString("assignee")).orElse("Unassigned");
    // String statu = Optional.ofNullable(doc.getString("status")).orElse("UNKNOWN");
    // long count = ((Number) doc.get("count")).longValue();
    //
    // assigneeStatusCounts.computeIfAbsent(assignee, k -> new HashMap<>()).put(statu, count);
    // }
    //
    // Map<String, Long> todayStatusCounts = parseSimpleCountMap((List<Document>) resultDoc.get("todayStatusCounts"));
    //
    // List<IssueEntity> latestIssues = ((List<Document>) resultDoc.get("latestIssues")).stream()
    // .map(doc -> mongoTemplate.getConverter().read(IssueEntity.class, doc)).toList();
    //
    // long overdueCount = Optional.ofNullable(resultDoc.getList("overdueIssues", Document.class))
    // .flatMap(list -> list.stream().findFirst()).map(doc -> ((Number) doc.get("count")).longValue())
    // .orElse(0L);
    //
    // double avgTimeToClose = Optional.ofNullable(resultDoc.getList("avgTimeToClose", Document.class))
    // .flatMap(list -> list.stream().findFirst()).map(doc -> ((Number) doc.get("avgDuration")).doubleValue())
    // .orElse(0.0);
    // long slaBreaches = Optional.ofNullable(resultDoc.getList("slaBreachedCount", Document.class))
    // .flatMap(list -> list.stream().findFirst()).map(doc -> ((Number) doc.get("count")).longValue())
    // .orElse(0L);
    //
    // // Map<String, Map<String, Long>> trendPerDay = parseTrend(resultDoc.getList("statusTrendPerDay",
    // // Document.class), "day");
    // // Map<String, Map<String, Long>> weeklyTrend = parseTrend(resultDoc.getList("statusTrendPerWeek",
    // // Document.class), "week");
    // // Map<String, Map<String, Long>> monthlyTrend = parseTrend(resultDoc.getList("statusTrendPerMonth",
    // // Document.class), "month");
    //
    // return IssueAnalyticsDTO.builder().todayStatusCountsGlobal(totalStatusCounts)
    // .assigneeStatusCounts(assigneeStatusCounts).todayStatusCounts(todayStatusCounts)
    // .latestIssues(latestIssues).overdueIssueCount(overdueCount).avgTimeToCloseSeconds(avgTimeToClose)
    // // .statusTrendPerDay(trendPerDay)
    // // .statusTrendPerWeek(weeklyTrend)
    // // .statusTrendPerMonth(monthlyTrend)
    // .slaBreachedCount(slaBreaches).build();
    //
    // }

    private Aggregation withOptionalMatch(Criteria criteria, AggregationOperation... rest) {
        if (criteria == null || criteria.getCriteriaObject().isEmpty()) {
            return Aggregation.newAggregation(rest);
        } else {
            AggregationOperation match = Aggregation.match(criteria);
            List<AggregationOperation> operations = new ArrayList<>();
            operations.add(match);
            operations.addAll(Arrays.asList(rest));
            return Aggregation.newAggregation(operations);
        }
    }

    @Override
    public IssueAnalyticsDTO getIssueAnalytics(String assigneeId, String status, String priority, String category,
            Long startDate, Long endDate, int page, int size) {
        long startOfToday = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        log.info("getIssueAnalytics {}", assigneeId);

        // Build dynamic filters
        List<Criteria> filters = new ArrayList<>();
        if (assigneeId != null && !assigneeId.isEmpty())
            filters.add(Criteria.where("assignee").is(assigneeId));
        if (status != null && !status.isEmpty())
            filters.add(Criteria.where("issue_status").is(status));
        if (priority != null && !priority.isEmpty())
            filters.add(Criteria.where("priority").is(priority));
        if (category != null && !category.isEmpty())
            filters.add(Criteria.where("category").is(category));
        if (startDate != null)
            filters.add(Criteria.where("created_at").gte(startDate));
        if (endDate != null)
            filters.add(Criteria.where("created_at").lte(endDate));

        Criteria dynamicCriteria = filters.isEmpty() ? null
                : new Criteria().andOperator(filters.toArray(new Criteria[0]));

        // Helper to build aggregation pipeline with optional match
        Function<AggregationOperation[], Aggregation> aggWithMatch = (steps) -> {
            List<AggregationOperation> ops = new ArrayList<>();
            if (dynamicCriteria != null)
                ops.add(Aggregation.match(dynamicCriteria));
            ops.addAll(Arrays.asList(steps));
            return Aggregation.newAggregation(ops);
        };

        // 1. Status count
        Aggregation statusAgg = Aggregation.newAggregation(Aggregation.group("issue_status").count().as("count"));

        // 2. Assignee + Status grouping
        Aggregation assigneeStatusAgg = aggWithMatch.apply(
                new AggregationOperation[] { Aggregation.group("assignee", "issue_status").count().as("count") });

        // 3. Today's status (extend filter to include start of today)
        Criteria todayCriteria = dynamicCriteria == null ? Criteria.where("created_at").gte(startOfToday)
                : new Criteria().andOperator(dynamicCriteria, Criteria.where("created_at").gte(startOfToday));
        Aggregation todayStatusAgg = Aggregation.newAggregation(Aggregation.match(todayCriteria),
                Aggregation.group("issue_status").count().as("count"));

        // 4. Priority vs Status
        Aggregation priorityStatusAgg = aggWithMatch.apply(
                new AggregationOperation[] { Aggregation.group("priority", "issue_status").count().as("count") });

        // 5. Reopened count
        Criteria reopenedCriteria = dynamicCriteria == null ? Criteria.where("reopen_count").gt(0)
                : new Criteria().andOperator(dynamicCriteria, Criteria.where("reopen_count").gt(0));
        Aggregation reopenedAgg = Aggregation.newAggregation(Aggregation.match(reopenedCriteria),
                Aggregation.count().as("reopened"));

        // 6. SLA breached
        Criteria slaCriteria = dynamicCriteria == null
                ? new Criteria().andOperator(Criteria.where("due_date").lt(System.currentTimeMillis()),
                        Criteria.where("issue_status").ne("CLOSED"))
                : new Criteria().andOperator(dynamicCriteria, Criteria.where("due_date").lt(System.currentTimeMillis()),
                        Criteria.where("issue_status").ne("CLOSED"));
        Aggregation slaAgg = Aggregation.newAggregation(Aggregation.match(slaCriteria),
                Aggregation.count().as("slaBreached"));

        // 7. Tag usage
        Aggregation tagsAgg = aggWithMatch.apply(
                new AggregationOperation[] { Aggregation.unwind("tags"), Aggregation.group("tags").count().as("count"),
                        Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")), Aggregation.limit(10) });

        // 8. Latest issues
        // Aggregation latestAgg = aggWithMatch.apply(new AggregationOperation[] {
        // Aggregation.sort(Sort.by(Sort.Direction.DESC, "created_at")), Aggregation.limit(100) });
        long totalCount = mongoTemplate.count(Query.query(dynamicCriteria), IssueEntity.class);
        int skip = page * size;
        int totalPages = (int) ((totalCount + size - 1) / size);
        Aggregation latestAgg = Aggregation.newAggregation(Aggregation.match(dynamicCriteria),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "created_at")), Aggregation.skip(skip),
                Aggregation.limit(size));

        List<IssueEntity> latest = mongoTemplate.aggregate(latestAgg, "issue_data", IssueEntity.class)
                .getMappedResults();

        // Global Status Count (Unfiltered)
        Aggregation statusGlobalAgg = Aggregation.newAggregation(Aggregation.group("issue_status").count().as("count"));

        // Global Today's Status (Unfiltered)
        Aggregation todayGlobalAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("created_at").gte(startOfToday)),
                Aggregation.group("issue_status").count().as("count"));

        // Global Priority vs Status (Unfiltered)
        Aggregation priorityGlobalAgg = Aggregation
                .newAggregation(Aggregation.group("priority", "issue_status").count().as("count"));

        // Global SLA breached (Unfiltered)
        Aggregation slaGlobalAgg = Aggregation.newAggregation(
                Aggregation.match(
                        Criteria.where("due_date").lt(System.currentTimeMillis()).and("issue_status").ne("CLOSED")),
                Aggregation.count().as("slaBreached"));

        Aggregation divisionAgg = Aggregation.newAggregation(Aggregation.match(dynamicCriteria), // filtered by user
                                                                                                 // input
                Aggregation.group("division_id").count().as("count"));

        // Run and parse
        Map<String, Long> statusCounts = toMap(mongoTemplate.aggregate(statusAgg, "issue_data", Document.class));
        Map<String, Map<String, Long>> assigneeStatus = toNestedMap(
                mongoTemplate.aggregate(assigneeStatusAgg, "issue_data", Document.class));
        Map<String, Long> todayCounts = toMap(mongoTemplate.aggregate(todayStatusAgg, "issue_data", Document.class));
        List<PriorityStatusCountDTO> priorityStatus = toPriorityStatus(
                mongoTemplate.aggregate(priorityStatusAgg, "issue_data", Document.class));
        long reopened = getCountFromSingle(mongoTemplate.aggregate(reopenedAgg, "issue_data", Document.class),
                "reopened");
        long sla = getCountFromSingle(mongoTemplate.aggregate(slaAgg, "issue_data", Document.class), "slaBreached");
        List<TagUsageDTO> tags = toTagList(mongoTemplate.aggregate(tagsAgg, "issue_data", Document.class));
        // List<IssueEntity> latest = mongoTemplate.aggregate(latestAgg, "issue_data", IssueEntity.class)
        // .getMappedResults();
        Map<String, Long> statusCountsGlobal = toMap(
                mongoTemplate.aggregate(statusGlobalAgg, "issue_data", Document.class));
        Map<String, Long> todayStatusCountsGlobal = toMap(
                mongoTemplate.aggregate(todayGlobalAgg, "issue_data", Document.class));
        List<PriorityStatusCountDTO> priorityStatusGlobal = toPriorityStatus(
                mongoTemplate.aggregate(priorityGlobalAgg, "issue_data", Document.class));
        long slaBreachedCountGlobal = getCountFromSingle(
                mongoTemplate.aggregate(slaGlobalAgg, "issue_data", Document.class), "slaBreached");
        Map<String, Long> rawDivisionCounts = toMap(mongoTemplate.aggregate(divisionAgg, "issue_data", Document.class));
        Map<String, Long> divisionWiseCounts = rawDivisionCounts.entrySet().stream().collect(Collectors.toMap(entry -> {
            String divisionId = entry.getKey();
            String name = divisionLoginService.getDivisionFromId(divisionId);
            return name + "_" + divisionId;
        }, Map.Entry::getValue));

        PagedResult<IssueEntity> pagedLatest = PagedResult.<IssueEntity> builder().content(latest)
                .totalElements(totalCount).totalPages(totalPages).page(page).size(size).build();

        return IssueAnalyticsDTO.builder().assigneeStatusCounts(assigneeStatus).todayStatusCounts(todayCounts)
                .priorityStatusCounts(priorityStatus).reopenedIssuesCount(reopened).slaBreachedCount(sla)
                // .latestIssues(latest).popularTags(tags)
                .divisionWiseCounts(divisionWiseCounts) // ✅ new field
                .pagedLatestIssues(pagedLatest)

                // ✅ Global values
                .statusCountsGlobal(statusCountsGlobal).todayStatusCountsGlobal(todayStatusCountsGlobal)
                .priorityStatusCountsGlobal(priorityStatusGlobal).slaBreachedCountGlobal(slaBreachedCountGlobal)
                .name(divisionLoginService.getDivisionFromId(assigneeId)).build();
    }

    // @Override
    // public Optional<List<WMessageEntity>> getWhatsAppSkippedMsg() {
    // return Optional.of(wMessageRepository.findByActiveStatusTrueAndIsIssueFalse());
    // }

    @Override
    public Optional<List<WMessageEntity>> getWhatsAppSkippedMsg() {
        long twoWeekAgoMillis = System.currentTimeMillis() - (14L * 24 * 60 * 60 * 1000);
        return Optional
                .of(wMessageRepository.findByActiveStatusTrueAndIsIssueFalseAndPostTimeGreaterThan(twoWeekAgoMillis));
    }

    @Override
    public List<IssueCategory> getCategories() {
        return issueCategoryRepository.findAll();

    }

    @Override
    public String restoreUpIssueFromSkip(String issueId, String userId) {
        // Fetch the entity by rdpsId
        Optional<WMessageEntity> wMessageEntityOptional = wMessageRepository.findById(issueId);

        if (wMessageEntityOptional.isPresent()) {
            WMessageEntity wMessageEntity = wMessageEntityOptional.get();
            wMessageEntity.setUpdatedBy(userId);
            wMessageEntity.setActionBy(userId);
            wMessageEntity.setUpdatedAt(System.currentTimeMillis() / 1000);

            // Set active_status too false to perform a soft delete
            wMessageEntity.setActiveStatus(true);
            wMessageEntity.setIsIssue(true);

            wMessageRepository.save(wMessageEntity);

        } else {
            // Handle the case where the rdpsId does not exist
            return "Record with issueId " + issueId + " not found.";
        }
        return issueId;
    }

    private Map<String, Long> parseSimpleCountMap(List<Document> docs) {
        return docs.stream().filter(doc -> doc.get("_id") != null || doc.get("issue_status") != null)
                .collect(Collectors.toMap(
                        doc -> String.valueOf(doc.get("_id") != null ? doc.get("_id") : doc.get("issue_status")),
                        doc -> ((Number) doc.get("count")).longValue()));
    }
    // Weekly and Monthly Trends:
    // private Map<String, Map<String, Long>> parseTrend(List<Document> docs, String key) {
    // Map<String, Map<String, Long>> result = new TreeMap<>();
    // for (Document doc : docs) {
    // Document id = (Document) doc.get("_id");
    // String period = id.getString(key);
    // String status = id.getString("status");
    // long count = ((Number) doc.get("count")).longValue();
    //
    // result.computeIfAbsent(period, k -> new HashMap<>()).put(status, count);
    // }
    // return result;
    // }

    private Map<String, Map<String, Long>> parseTrend(List<Document> docs, String timeKey) {
        Map<String, Map<String, Long>> trend = new TreeMap<>();
        if (docs != null) {
            for (Document d : docs) {
                Document id = (Document) d.get("_id");
                if (id != null) {
                    String time = id.getString(timeKey);
                    String status = id.getString("status");
                    long count = ((Number) d.get("count")).longValue();

                    trend.computeIfAbsent(time, k -> new HashMap<>()).put(status, count);
                }
            }
        }
        return trend;
    }

    private void compareField(String field, Object oldVal, Object newVal, List<UpdateAuditLog> changes,
            String updatedBy, Long timestamp) {
        String oldStr = oldVal != null ? oldVal.toString() : null;
        String newStr = newVal != null ? newVal.toString() : null;

        if (!Objects.equals(oldStr, newStr)) {
            changes.add(new UpdateAuditLog(field, oldStr, newStr, updatedBy, timestamp));
        }
    }

    private Map<String, Long> toMap(AggregationResults<Document> result) {
        return result.getMappedResults().stream().filter(doc -> doc.get("_id") != null && doc.get("count") != null) // 💡
                                                                                                                    // Filter
                                                                                                                    // out
                                                                                                                    // nulls
                .collect(Collectors.toMap(doc -> String.valueOf(doc.get("_id")), // 💡 Safe conversion to string
                        doc -> ((Number) doc.get("count")).longValue()));
    }

    private Map<String, Map<String, Long>> toNestedMap(AggregationResults<Document> result) {
        Map<String, Map<String, Long>> nestedMap = new HashMap<>();
        for (Document doc : result.getMappedResults()) {
            Object rawId = doc.get("_id");

            if (!(rawId instanceof Document idDoc)) {
                log.warn("Unexpected _id format or null: {}", doc.toJson());
                continue; // skip malformed documents
            }

            String assignee = idDoc.getString("assignee");
            String status = idDoc.getString("issue_status");
            long count = ((Number) doc.get("count")).longValue();

            // Fallback keys
            String safeAssignee = assignee != null ? assignee : "Unassigned";
            String safeStatus = status != null ? status : "Unknown";

            nestedMap.computeIfAbsent(safeAssignee, k -> new HashMap<>()).put(safeStatus, count);
        }
        return nestedMap;
    }

    private List<PriorityStatusCountDTO> toPriorityStatus(AggregationResults<Document> result) {
        return result.getMappedResults().stream().map(doc -> {
            Document id = (Document) doc.get("_id");
            long count = ((Number) doc.get("count")).longValue();
            return new PriorityStatusCountDTO(id.getString("priority"), id.getString("issue_status"), count);
        }).toList();
    }

    private long getCountFromSingle(AggregationResults<Document> result, String field) {
        List<Document> docs = result.getMappedResults();
        return docs.isEmpty() ? 0L : ((Number) docs.get(0).get(field)).longValue();
    }

    private List<TagUsageDTO> toTagList(AggregationResults<Document> result) {
        return result.getMappedResults().stream()
                .map(doc -> new TagUsageDTO(doc.getString("_id"), ((Number) doc.get("count")).longValue())).toList();
    }

}
