package com.primesys.adminserviceserver.service;

import com.primesys.adminservicecommon.dto.CreateIssueRequest;
import com.primesys.adminservicecommon.dto.IssueAnalyticsDTO;
import com.primesys.adminservicemongodb.entity.IssueCategory;
import com.primesys.adminservicemongodb.entity.IssueEntity;
import com.primesys.adminservicemongodb.entity.WMessageEntity;
import com.primesys.adminservicemongodb.model.UpdateIssueRequest;
import com.primesys.adminserviceserver.request.AddCommentRequest;
import com.primesys.adminserviceserver.request.DeleteCommentRequest;
import com.primesys.adminserviceserver.request.EditCommentRequest;
import com.primesys.adminserviceserver.request.MessageRequest;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.response.IssueResponseDto;

import java.util.List;
import java.util.Optional;

public interface IssueService {

    Optional<List<WMessageEntity>> saveMsg(List<MessageRequest> msgList);

    Optional<List<WMessageEntity>> getWhatsAppMsg();

    String pickUpIssue(String noteId, String userId, String action);

    Optional<List<IssueResponseDto>> getIssuePickByMember(String userId);

    IssueEntity transferIssue(String issueId, String newAssignee, String transferredBy, String reason);

    IssueEntity createIssue(CreateIssueRequest request);

    IssueEntity updateIssue(UpdateIssueRequest request);

    Optional<FileUploadResultResponse> uploadIssueAttachmentFile(String col, String s, String updatedBy);

    IssueEntity addComment(AddCommentRequest request);

    IssueEntity editComment(EditCommentRequest request);

    IssueEntity deleteComment(DeleteCommentRequest request);

    Object getGroupedSummary(List<String> groupFields);

    IssueAnalyticsDTO getIssueAnalytics(String assigneeId, String status, String priority, String category,
            Long startDate, Long endDate, int page, int size);

    Optional<List<WMessageEntity>> getWhatsAppSkippedMsg();

    List<IssueCategory> getCategories();

    String restoreUpIssueFromSkip(String issueId, String userId);

    // to check if the issue has been already picked or not
    boolean isIssuePickable(String noteId);

    // IssueAnalyticsDTO getIssueAnalyticsFiltered(String assigneeId, String status, String priority, String category,
    // Long startDate, Long endDate, int page, int size);

    // IssueAnalyticsDTO getIssueAnalytics(String assigneeId, String status, String priority, String category,
    // Long startDate, Long endDate);
}
