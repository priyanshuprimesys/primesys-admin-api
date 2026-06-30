package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.IssueSkipMsgEntity;
import com.primesys.adminservicemongodb.entity.IssueTicketEntity;
import com.primesys.adminservicemongodb.entity.TransferLog;
import com.primesys.adminserviceserver.dtos.issue.ActivityEntry;
import com.primesys.adminserviceserver.dtos.issue.TicketStatsDto;
import com.primesys.adminserviceserver.dtos.issue.TransferMemberDto;
import com.primesys.adminserviceserver.request.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface IssueTicketService {

    // ─── Read ─────────────────────────────────────────────────────────────────

    List<IssueTicketEntity> getAllTickets();

    Optional<IssueTicketEntity> getTicketById(String id);

    Optional<IssueTicketEntity> getTicketByTicketId(String ticketId);

    List<IssueTicketEntity> getTicketsByStatus(String status);

    List<IssueTicketEntity> getTicketsByDivision(String divisionId);

    List<IssueTicketEntity> getTicketsByGroup(String groupName);

    List<IssueTicketEntity> getTicketsByPriority(String priority);

    List<IssueTicketEntity> getTicketsByAssignee(String assignee);

    List<IssueTicketEntity> getTicketsByReporter(String reporter);

    List<IssueTicketEntity> getTicketsByWatcher(String userId);

    // ─── Search / Paged ───────────────────────────────────────────────────────

    List<IssueTicketEntity> searchTickets(String status, String priority, String assignee, String divisionId,
            String group, String reporter, Long from, Long to);

    Page<IssueTicketEntity> getPagedTickets(String status, String priority, String assignee, String divisionId,
            Pageable pageable);

    // ─── Create ───────────────────────────────────────────────────────────────

    IssueTicketEntity createTicket(CreateIssueTicketRequest request);

    // ─── Update ───────────────────────────────────────────────────────────────

    IssueTicketEntity updateTicket(UpdateIssueTicketRequest request);

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    IssueTicketEntity changeStatus(String id, ChangeStatusRequest request);

    IssueTicketEntity resolveTicket(String id, ResolveTicketRequest request);

    IssueTicketEntity closeTicket(String id, CloseTicketRequest request);

    IssueTicketEntity reopenTicket(String id, ReopenTicketRequest request);

    // ─── Assignment ───────────────────────────────────────────────────────────

    String pickupTicket(String id, PickupTicketRequest request);

    IssueTicketEntity unassignTicket(String id, String unassignedBy);

    // ─── Transfer ─────────────────────────────────────────────────────────────

    IssueTicketEntity transferTicket(TransferTicketRequest request);

    List<TransferMemberDto> getTransferMembers();

    List<TransferLog> getTransferHistory(String ticketId);

    // ─── Comments ─────────────────────────────────────────────────────────────

    IssueTicketEntity addComment(AddCommentRequest request);

    IssueTicketEntity editComment(EditCommentRequest request);

    IssueTicketEntity deleteComment(DeleteCommentRequest request);

    // ─── Watch ────────────────────────────────────────────────────────────────

    IssueTicketEntity watchTicket(String id, String userId, String displayName);

    IssueTicketEntity unwatchTicket(String id, String userId);

    // ─── Linked Tickets ───────────────────────────────────────────────────────

    IssueTicketEntity linkTicket(String id, LinkTicketRequest request);

    IssueTicketEntity unlinkTicket(String id, String linkedTicketId);

    // ─── Attachments ──────────────────────────────────────────────────────────

    List<String> getAttachments(String id);

    String uploadAttachment(String ticketId, MultipartFile file, String updatedBy);

    IssueTicketEntity deleteAttachment(String id, String fileName, String deletedBy);

    // ─── Activity ─────────────────────────────────────────────────────────────

    List<ActivityEntry> getActivity(String id);

    // ─── Stats / Dashboard ────────────────────────────────────────────────────

    TicketStatsDto getStats(String divisionId);

    List<IssueTicketEntity> getOverdueTickets(String divisionId);

    // ─── Bulk ─────────────────────────────────────────────────────────────────

    List<IssueTicketEntity> bulkUpdateStatus(BulkStatusRequest request);

    List<IssueTicketEntity> bulkAssign(BulkAssignRequest request);

    // ─── Delete (soft) ────────────────────────────────────────────────────────

    IssueTicketEntity deleteTicket(String id, String deletedBy);

    // ─── Skip Messages ────────────────────────────────────────────────────────

    List<IssueSkipMsgEntity> getSkipMessages();

    IssueTicketEntity convertSkipMsgToTicket(String skipMsgId, String convertedBy);
}
