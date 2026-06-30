package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicemongodb.entity.IssueSkipMsgEntity;
import com.primesys.adminservicemongodb.entity.IssueTicketEntity;
import com.primesys.adminservicemongodb.entity.TransferLog;
import com.primesys.adminserviceserver.dtos.issue.ActivityEntry;
import com.primesys.adminserviceserver.dtos.issue.TicketStatsDto;
import com.primesys.adminserviceserver.dtos.issue.TransferMemberDto;
import com.primesys.adminserviceserver.request.*;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.IssueTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/issue-ticket")
@CrossOrigin("*")
public class IssueTicketController {

    private final IssueTicketService issueTicketService;

    // ─── Read ──────────────────────────────────────────────────────────────

    @GetMapping("/all")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getAllTickets() {
        log.info("get all issue tickets");
        return ok(issueTicketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> getTicketById(@PathVariable String id) {
        log.info("get issue ticket by id: {}", id);
        return issueTicketService.getTicketById(id).map(t -> ok(t)).orElse(notFound());
    }

    @GetMapping
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> getTicketByTicketId(@RequestParam String ticketId) {
        log.info("get issue ticket by ticketId: {}", ticketId);
        return issueTicketService.getTicketByTicketId(ticketId).map(t -> ok(t)).orElse(notFound());
    }

    @GetMapping("/by-status")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getTicketsByStatus(@RequestParam String status) {
        return ok(issueTicketService.getTicketsByStatus(status));
    }

    @GetMapping("/by-division")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getTicketsByDivision(
            @RequestParam String divisionId) {
        return ok(issueTicketService.getTicketsByDivision(divisionId));
    }

    @GetMapping("/by-group")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getTicketsByGroup(@RequestParam String groupName) {
        return ok(issueTicketService.getTicketsByGroup(groupName));
    }

    @GetMapping("/by-priority")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getTicketsByPriority(
            @RequestParam String priority) {
        return ok(issueTicketService.getTicketsByPriority(priority));
    }

    @GetMapping("/by-assignee")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getTicketsByAssignee(
            @RequestParam String assignee) {
        log.info("get tickets by assignee: {}", assignee);
        return ok(issueTicketService.getTicketsByAssignee(assignee));
    }

    @GetMapping("/by-reporter")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getTicketsByReporter(
            @RequestParam String reporter) {
        log.info("get tickets by reporter: {}", reporter);
        return ok(issueTicketService.getTicketsByReporter(reporter));
    }

    @GetMapping("/by-watcher")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getTicketsByWatcher(@RequestParam String userId) {
        log.info("get tickets watched by userId: {}", userId);
        return ok(issueTicketService.getTicketsByWatcher(userId));
    }

    // ─── Search / Paged ────────────────────────────────────────────────────

    /**
     * Combined filter — all params optional. GET
     * /v2/issue-ticket/search?status=OPEN,IN_PROGRESS&priority=HIGH&assignee=u1&divisionId=d1&group=g1&reporter=r1&from=1700000000000&to=1800000000000
     */
    @GetMapping("/search")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> searchTickets(
            @RequestParam(required = false) String status, @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assignee, @RequestParam(required = false) String divisionId,
            @RequestParam(required = false) String group, @RequestParam(required = false) String reporter,
            @RequestParam(required = false) Long from, @RequestParam(required = false) Long to) {
        log.info("search tickets status={} priority={} assignee={} divisionId={}", status, priority, assignee,
                divisionId);
        return ok(issueTicketService.searchTickets(status, priority, assignee, divisionId, group, reporter, from, to));
    }

    /**
     * Paginated list. GET
     * /v2/issue-ticket/paged?page=0&size=20&sort=postTime,desc&status=OPEN&priority=HIGH&assignee=u1&divisionId=d1
     */
    @GetMapping("/paged")
    public ResponseEntity<HttpApiResponse<Page<IssueTicketEntity>>> getPagedTickets(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "postTime") String sortBy, @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status, @RequestParam(required = false) String priority,
            @RequestParam(required = false) String assignee, @RequestParam(required = false) String divisionId) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        log.info("paged tickets page={} size={} status={}", page, size, status);
        return ok(issueTicketService.getPagedTickets(status, priority, assignee, divisionId, pageable));
    }

    // ─── Stats / Dashboard ─────────────────────────────────────────────────

    /**
     * Ticket counts grouped by status, priority, assignee. GET /v2/issue-ticket/stats?divisionId=xxx
     */
    @GetMapping("/stats")
    public ResponseEntity<HttpApiResponse<TicketStatsDto>> getStats(@RequestParam(required = false) String divisionId) {
        log.info("get ticket stats divisionId={}", divisionId);
        return ok(issueTicketService.getStats(divisionId));
    }

    /**
     * Tickets past their due date and not resolved/closed. GET /v2/issue-ticket/overdue?divisionId=xxx
     */
    @GetMapping("/overdue")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> getOverdueTickets(
            @RequestParam(required = false) String divisionId) {
        log.info("get overdue tickets divisionId={}", divisionId);
        return ok(issueTicketService.getOverdueTickets(divisionId));
    }

    // ─── Transfer ──────────────────────────────────────────────────────────

    @GetMapping("/{id}/transfer-history")
    public ResponseEntity<HttpApiResponse<List<TransferLog>>> getTransferHistory(@PathVariable String id) {
        log.info("get transfer history for ticket {}", id);
        return ok(issueTicketService.getTransferHistory(id));
    }

    @GetMapping("/transfer-members")
    public ResponseEntity<HttpApiResponse<List<TransferMemberDto>>> getTransferMembers() {
        log.info("get transfer members (role_id 19/20)");
        return ok(issueTicketService.getTransferMembers());
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> transferTicket(@PathVariable String id,
            @RequestBody TransferTicketRequest request) {
        log.info("transfer ticket {} to {}", id, request.getToAssignee());
        request.setId(id);
        return ok(issueTicketService.transferTicket(request));
    }

    // ─── Create ────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> createTicket(
            @RequestBody CreateIssueTicketRequest request) {
        log.info("create issue ticket: {}", request);
        IssueTicketEntity saved = issueTicketService.createTicket(request);
        return new ResponseEntity<>(new HttpApiResponse<>(saved, true), HttpStatus.CREATED);
    }

    // ─── Update ────────────────────────────────────────────────────────────

    @PutMapping
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> updateTicket(
            @RequestBody UpdateIssueTicketRequest request) {
        log.info("update issue ticket id: {}", request.getId());
        return ok(issueTicketService.updateTicket(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> updateTicketById(@PathVariable String id,
            @RequestBody UpdateIssueTicketRequest request) {
        log.info("update issue ticket id: {}", id);
        request.setId(id);
        return ok(issueTicketService.updateTicket(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> patchTicket(@PathVariable String id,
            @RequestBody UpdateIssueTicketRequest request) {
        log.info("patch issue ticket id: {}", id);
        request.setId(id);
        return ok(issueTicketService.updateTicket(request));
    }

    // ─── Lifecycle ─────────────────────────────────────────────────────────

    /**
     * Change status explicitly. PATCH /v2/issue-ticket/{id}/status Body: { status, updatedBy, note }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> changeStatus(@PathVariable String id,
            @RequestBody ChangeStatusRequest request) {
        log.info("change status ticket {} to {}", id, request.getStatus());
        return ok(issueTicketService.changeStatus(id, request));
    }

    /**
     * Resolve a ticket. POST /v2/issue-ticket/{id}/resolve Body: { resolution, resolvedBy, note }
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> resolveTicket(@PathVariable String id,
            @RequestBody ResolveTicketRequest request) {
        log.info("resolve ticket {} resolution={}", id, request.getResolution());
        return ok(issueTicketService.resolveTicket(id, request));
    }

    /**
     * Close a ticket. POST /v2/issue-ticket/{id}/close Body: { closedBy, note }
     */
    @PostMapping("/{id}/close")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> closeTicket(@PathVariable String id,
            @RequestBody CloseTicketRequest request) {
        log.info("close ticket {} by {}", id, request.getClosedBy());
        return ok(issueTicketService.closeTicket(id, request));
    }

    /**
     * Reopen a resolved/closed ticket. POST /v2/issue-ticket/{id}/reopen Body: { reopenedBy, note }
     */
    @PostMapping("/{id}/reopen")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> reopenTicket(@PathVariable String id,
            @RequestBody ReopenTicketRequest request) {
        log.info("reopen ticket {} by {}", id, request.getReopenedBy());
        return ok(issueTicketService.reopenTicket(id, request));
    }

    // ─── Assignment ────────────────────────────────────────────────────────

    @PostMapping("/{id}/pickup")
    public ResponseEntity<HttpApiResponse<String>> pickupTicket(@PathVariable String id,
            @RequestBody PickupTicketRequest request) {
        log.info("pickup ticket {} by user {}", id, request.getUserId());
        return new ResponseEntity<>(new HttpApiResponse<>(issueTicketService.pickupTicket(id, request)), HttpStatus.OK);
    }

    /**
     * Remove assignee from a ticket. DELETE /v2/issue-ticket/{id}/assignee?unassignedBy=xxx
     */
    @DeleteMapping("/{id}/assignee")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> unassignTicket(@PathVariable String id,
            @RequestParam String unassignedBy) {
        log.info("unassign ticket {} by {}", id, unassignedBy);
        return ok(issueTicketService.unassignTicket(id, unassignedBy));
    }

    // ─── Comments ──────────────────────────────────────────────────────────

    @PostMapping("/{id}/comment")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> addComment(@PathVariable String id,
            @RequestBody AddCommentRequest request) {
        log.info("add comment to ticket {}", id);
        request.setIssueId(id);
        return ok(issueTicketService.addComment(request));
    }

    @PutMapping("/{id}/comment")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> editComment(@PathVariable String id,
            @RequestBody EditCommentRequest request) {
        log.info("edit comment {} on ticket {}", request.getCommentId(), id);
        request.setIssueId(id);
        return ok(issueTicketService.editComment(request));
    }

    @DeleteMapping("/{id}/comment")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> deleteComment(@PathVariable String id,
            @RequestBody DeleteCommentRequest request) {
        log.info("delete comment {} on ticket {}", request.getCommentId(), id);
        request.setIssueId(id);
        return ok(issueTicketService.deleteComment(request));
    }

    // ─── Watch / Unwatch ───────────────────────────────────────────────────

    @PostMapping("/{id}/watch")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> watchTicket(@PathVariable String id,
            @RequestParam String userId, @RequestParam(required = false) String displayName) {
        log.info("user {} watching ticket {}", userId, id);
        return ok(issueTicketService.watchTicket(id, userId, displayName));
    }

    @DeleteMapping("/{id}/watch")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> unwatchTicket(@PathVariable String id,
            @RequestParam String userId) {
        log.info("user {} unwatching ticket {}", userId, id);
        return ok(issueTicketService.unwatchTicket(id, userId));
    }

    // ─── Linked Tickets ────────────────────────────────────────────────────

    /**
     * Link this ticket to another. POST /v2/issue-ticket/{id}/link Body: { linkedTicketId, linkType, linkedBy }
     */
    @PostMapping("/{id}/link")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> linkTicket(@PathVariable String id,
            @RequestBody LinkTicketRequest request) {
        log.info("link ticket {} to {}", id, request.getLinkedTicketId());
        return ok(issueTicketService.linkTicket(id, request));
    }

    /**
     * Remove a link between tickets. DELETE /v2/issue-ticket/{id}/link/{linkedTicketId}
     */
    @DeleteMapping("/{id}/link/{linkedTicketId}")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> unlinkTicket(@PathVariable String id,
            @PathVariable String linkedTicketId) {
        log.info("unlink ticket {} from {}", id, linkedTicketId);
        return ok(issueTicketService.unlinkTicket(id, linkedTicketId));
    }

    // ─── Attachments ───────────────────────────────────────────────────────

    /**
     * List all attachments for a ticket. GET /v2/issue-ticket/{id}/attachments
     */
    @GetMapping("/{id}/attachments")
    public ResponseEntity<HttpApiResponse<List<String>>> getAttachments(@PathVariable String id) {
        log.info("get attachments for ticket {}", id);
        return ok(issueTicketService.getAttachments(id));
    }

    @PostMapping(value = "/upload-attachment", consumes = { "multipart/form-data", "application/json" })
    public ResponseEntity<HttpApiResponse<Object>> uploadAttachment(@RequestParam("file") MultipartFile file,
            @RequestParam("ticketId") String ticketId, @RequestParam("updatedBy") String updatedBy) {

        if (file.isEmpty()) {
            return new ResponseEntity<>(new HttpApiResponse<>(new ErrorResponse(1001, "File is empty")), HttpStatus.OK);
        }

        try {
            String filePath = issueTicketService.uploadAttachment(ticketId, file, updatedBy);
            return new ResponseEntity<>(new HttpApiResponse<>(filePath, Boolean.TRUE), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Attachment upload error for ticket {}: {}", ticketId, e.getMessage());
            return new ResponseEntity<>(new HttpApiResponse<>(new ErrorResponse(1005, e.getMessage())), HttpStatus.OK);
        }
    }

    /**
     * Remove an attachment from a ticket. DELETE
     * /v2/issue-ticket/{id}/attachment?fileName=issue_attachments/foo.png&deletedBy=xxx
     */
    @DeleteMapping("/{id}/attachment")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> deleteAttachment(@PathVariable String id,
            @RequestParam String fileName, @RequestParam String deletedBy) {
        log.info("delete attachment {} from ticket {} by {}", fileName, id, deletedBy);
        return ok(issueTicketService.deleteAttachment(id, fileName, deletedBy));
    }

    // ─── Activity ──────────────────────────────────────────────────────────

    /**
     * Full activity timeline for a ticket (status changes, comments, transfers), newest first. GET
     * /v2/issue-ticket/{id}/activity
     */
    @GetMapping("/{id}/activity")
    public ResponseEntity<HttpApiResponse<List<ActivityEntry>>> getActivity(@PathVariable String id) {
        log.info("get activity for ticket {}", id);
        return ok(issueTicketService.getActivity(id));
    }

    // ─── Bulk ──────────────────────────────────────────────────────────────

    /**
     * Update status for multiple tickets at once. POST /v2/issue-ticket/bulk/status Body: { ids: [...], status,
     * updatedBy }
     */
    @PostMapping("/bulk/status")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> bulkUpdateStatus(
            @RequestBody BulkStatusRequest request) {
        log.info("bulk status update {} tickets to {}", request.getIds() != null ? request.getIds().size() : 0,
                request.getStatus());
        return ok(issueTicketService.bulkUpdateStatus(request));
    }

    /**
     * Assign multiple tickets to one person at once. POST /v2/issue-ticket/bulk/assign Body: { ids: [...], assignee,
     * assigneeName, assignedBy }
     */
    @PostMapping("/bulk/assign")
    public ResponseEntity<HttpApiResponse<List<IssueTicketEntity>>> bulkAssign(@RequestBody BulkAssignRequest request) {
        log.info("bulk assign {} tickets to {}", request.getIds() != null ? request.getIds().size() : 0,
                request.getAssignee());
        return ok(issueTicketService.bulkAssign(request));
    }

    // ─── Delete (soft) ─────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> deleteTicket(@PathVariable String id,
            @RequestParam String deletedBy) {
        log.info("soft-delete ticket {} by {}", id, deletedBy);
        return ok(issueTicketService.deleteTicket(id, deletedBy));
    }

    // ─── Skip Messages ─────────────────────────────────────────────────────

    @GetMapping("/skip-messages")
    public ResponseEntity<HttpApiResponse<List<IssueSkipMsgEntity>>> getSkipMessages() {
        log.info("get all skip messages");
        return ok(issueTicketService.getSkipMessages());
    }

    @PostMapping("/skip-messages/{id}/convert")
    public ResponseEntity<HttpApiResponse<IssueTicketEntity>> convertSkipMsgToTicket(@PathVariable String id,
            @RequestParam String convertedBy) {
        log.info("convert skip message {} to ticket by {}", id, convertedBy);
        return new ResponseEntity<>(
                new HttpApiResponse<>(issueTicketService.convertSkipMsgToTicket(id, convertedBy), true),
                HttpStatus.CREATED);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private <T> ResponseEntity<HttpApiResponse<T>> ok(T data) {
        return new ResponseEntity<>(new HttpApiResponse<>(data), HttpStatus.OK);
    }

    private ResponseEntity<HttpApiResponse<IssueTicketEntity>> notFound() {
        return new ResponseEntity<>(new HttpApiResponse<>((IssueTicketEntity) null, false), HttpStatus.NOT_FOUND);
    }
}
