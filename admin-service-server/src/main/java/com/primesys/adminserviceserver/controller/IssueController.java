package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicecommon.dto.CreateIssueRequest;
import com.primesys.adminservicecommon.dto.IssueAnalyticsDTO;
import com.primesys.adminservicecommon.error.message.ErrorCode;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.IssueCategory;
import com.primesys.adminservicemongodb.entity.IssueEntity;
import com.primesys.adminservicemongodb.entity.WMessageEntity;
import com.primesys.adminservicemongodb.model.UpdateIssueRequest;
import com.primesys.adminserviceserver.dtos.issue.CheckIssuePickableDTO;
import com.primesys.adminserviceserver.request.AddCommentRequest;
import com.primesys.adminserviceserver.request.DeleteCommentRequest;
import com.primesys.adminserviceserver.request.EditCommentRequest;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.response.HttpChatBotApiResponse;
import com.primesys.adminserviceserver.response.IssueResponseDto;
import com.primesys.adminserviceserver.responseHandler.ResponseHandler;
import com.primesys.adminserviceserver.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/issue")
@CrossOrigin("*")
public class IssueController {
    private final IssueService issueService;

    @GetMapping("/get-whatsapp-msg")
    HttpChatBotApiResponse<List<WMessageEntity>> getWhatsAppMsg() {
        log.info("get-questions call--");

        final Optional<List<WMessageEntity>> deviceDtos = issueService.getWhatsAppMsg();
        final HttpChatBotApiResponse<List<WMessageEntity>> response = new HttpChatBotApiResponse<>(
                Collections.singletonList(deviceDtos.get()));
        return response;
    }

    @GetMapping("/get-whatsapp-skipped-msg")
    HttpChatBotApiResponse<List<WMessageEntity>> getWhatsAppSkippedMsg() {
        log.info("get-skipped call--");

        final Optional<List<WMessageEntity>> deviceDtos = issueService.getWhatsAppSkippedMsg();
        final HttpChatBotApiResponse<List<WMessageEntity>> response = new HttpChatBotApiResponse<>(
                Collections.singletonList(deviceDtos.get()));
        // log.info("get-whatsapp- is {}", response);
        return response;
    }

    @PatchMapping("/pick-up")
    public ResponseEntity<HttpApiResponse<String>> pickUpIssue(@RequestParam final String noteId,
            @RequestParam final String userId, @RequestParam final String action) {
        log.info("Received request to pick  data with noteId: {}", noteId);

        // Perform the soft delete operation and get the result message
        String resultMessage = issueService.pickUpIssue(noteId, userId, action);

        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(resultMessage, Boolean.TRUE);
        if (!resultMessage.contains("successfully"))
            httpApiResponse.setSuccess(Boolean.FALSE);
        log.info("pickUpIssue response: {}", resultMessage);

        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @GetMapping("/is-pickable")
    public ResponseEntity<Map<String, Object>> isPickable(
            @Valid @ModelAttribute CheckIssuePickableDTO issuePickableDTO) {
        log.info("here is issue pickable");
        boolean isIssuePicked = issueService.isIssuePickable(issuePickableDTO.noteId());
        String message = !isIssuePicked ? "You can pick this issue" : "Issue has already been picked";
        return ResponseHandler.generateResponse(message, !isIssuePicked, HttpStatus.OK);
    }

    @GetMapping("/get-issue-of-member")
    HttpChatBotApiResponse<List<IssueResponseDto>> getIssuePickByMember(@RequestParam final String userId) {
        log.info("get-get-issue-of-member call--");

        final Optional<List<IssueResponseDto>> deviceDtos = issueService.getIssuePickByMember(userId);
        final HttpChatBotApiResponse<List<IssueResponseDto>> response = new HttpChatBotApiResponse<>(deviceDtos.get());
        // log.info("get-whatsapp- is {}", response);
        return response;
    }

    @PostMapping("/transfer")
    public HttpApiResponse<?> transferIssue(@RequestParam String issueId, @RequestParam String newAssignee,
            @RequestParam String transferredBy, @RequestParam(required = false) String reason) {
        log.info("/issues/transfer call--");

        IssueEntity divisionLogins = issueService.transferIssue(issueId, newAssignee, transferredBy, reason);
        HttpApiResponse<IssueEntity> result = new HttpApiResponse<>(divisionLogins);
        return result;
    }

    @PostMapping("/create-issue")
    public ResponseEntity<?> createIssue(@RequestBody CreateIssueRequest request) {
        log.info("Received request createIssue: {}", request);

        IssueEntity savedIssue = issueService.createIssue(request);
        // Prepare the response with the result message
        HttpApiResponse<IssueEntity> httpApiResponse = new HttpApiResponse<>(savedIssue, Boolean.TRUE);
        log.info("create-issue response: {}", httpApiResponse);

        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Object> updateIssue(@RequestBody UpdateIssueRequest request) {
        IssueEntity savedIssue = issueService.updateIssue(request);
        // Prepare the response with the result message
        HttpApiResponse<IssueEntity> httpApiResponse = new HttpApiResponse<>(savedIssue, Boolean.TRUE);
        log.info("create-issue response: {}", httpApiResponse);

        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

    }

    @PostMapping(value = "/upload-issue-attachment-file", consumes = { "multipart/form-data", "application/json" })
    public ResponseEntity<HttpApiResponse<Object>> uploadIssueAttachmentFile(@RequestParam("file") MultipartFile file,
            @RequestParam("updatedBy") String updatedBy) {
        if (file.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse(ErrorCode.EMPTY_FILE);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }

        // Specify the server location where you want to save the file
        // String serverLocation = "D:\\issue_attachments";
        String serverLocation = "/home/issue_attachments";

        String originalName = file.getOriginalFilename();
        String baseName = FilenameUtils.getBaseName(originalName); // Removes extension safely
        String normalizedBaseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "_"); // Allow only safe characters
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = normalizedBaseName + "_" + timestamp;

        try {
            // Get the file bytes
            // byte[] bytes = file.getBytes();

            // Create the directory if it doesn't exist
            File directory = new File(serverLocation);
            if (!directory.exists()) {
                directory.mkdirs(); // creates parent directories as well
            }

            // Create the file on the server
            File serverFile = new File(directory.getAbsolutePath() + File.separator + fileName + "."
                    + FilenameUtils.getExtension(originalName));
            file.transferTo(serverFile);
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>("issue_attachments/" + serverFile.getName(),
                    Boolean.TRUE);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error while up-loading  a file : {} :: error message : {}", file.getOriginalFilename(),
                    e.getMessage());
            e.printStackTrace();
            ErrorResponse errorResponse = new ErrorResponse(1005, e.getMessage());
            HttpApiResponse<Object> httpApiResponse = new HttpApiResponse<>(errorResponse);
            return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
        }

    }

    @PostMapping("/comment")
    public ResponseEntity<IssueEntity> postComment(@Valid @RequestBody AddCommentRequest request) {
        IssueEntity updatedIssue = issueService.addComment(request);

        return ResponseEntity.ok(updatedIssue);
    }

    @PutMapping("/comment")
    public ResponseEntity<HttpApiResponse<IssueEntity>> editComment(@RequestBody EditCommentRequest request) {
        IssueEntity updatedIssue = issueService.editComment(request);
        HttpApiResponse<IssueEntity> response = new HttpApiResponse<>(updatedIssue);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comment")
    public ResponseEntity<IssueEntity> deleteComment(@RequestBody DeleteCommentRequest request) {
        return ResponseEntity.ok(issueService.deleteComment(request));
    }

    @GetMapping("/summary")
    public ResponseEntity<Object> getGroupedSummary(@RequestParam String groupBy) {
        List<String> groupFields = Arrays.asList(groupBy.split(","));
        return ResponseEntity.ok(issueService.getGroupedSummary(groupFields));

    }

    @GetMapping("/issue-analytics")
    public HttpApiResponse<IssueAnalyticsDTO> getIssueAnalytics(@RequestParam(required = false) String assigneeId,
            @RequestParam(required = false) String status, @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category, @RequestParam(required = false) Long startDate,
            @RequestParam(required = false) Long endDate, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        IssueAnalyticsDTO data = issueService.getIssueAnalytics(assigneeId, status, priority, category, startDate,
                endDate, page, size);
        log.info("get-analytics- is {}", data);

        HttpApiResponse<IssueAnalyticsDTO> result = new HttpApiResponse<>(data);
        return result;

    }

    // @GetMapping("/analytics-filter")
    // public HttpApiResponse<IssueAnalyticsDTO> getFilteredAnalytics(@RequestParam(required = false) String assigneeId,
    // @RequestParam(required = false) String status, @RequestParam(required = false) String priority,
    // @RequestParam(required = false) String category, @RequestParam(required = false) Long startDate, // epoch
    // // seconds
    // @RequestParam(required = false) Long endDate ,// epoch seconds,
    // @RequestParam int page,@RequestParam int size
    // ) {
    // IssueAnalyticsDTO data = issueService.getIssueAnalyticsFiltered(assigneeId, status, priority, category,
    // startDate, endDate,page,size);
    // log.info("get-analytics- is {}", data);
    //
    // HttpApiResponse<IssueAnalyticsDTO> result = new HttpApiResponse<>(data);
    // return result;
    //
    // }
    @GetMapping("/issue-categories")
    public HttpApiResponse<List<IssueCategory>> getCategories() {
        List<IssueCategory> data = issueService.getCategories();
        log.info("Fetched issue categories: {}", data);
        return new HttpApiResponse<>(data);
    }

    @PatchMapping("/restore-as-issue")
    public ResponseEntity<HttpApiResponse<String>> restoreUpIssueFromSkip(@RequestParam final String issueId,
            @RequestParam final String userId) {
        log.info("Received request to restoreUpIssueFromSkip  data with issueId: {}", issueId);

        // Perform the soft delete operation and get the result message
        String resultMessage = issueService.restoreUpIssueFromSkip(issueId, userId);

        // Prepare the response with the result message
        HttpApiResponse<String> httpApiResponse = new HttpApiResponse<>(resultMessage, Boolean.TRUE);
        log.info("restoreUpIssueFromSkip response: {}", resultMessage);

        return new ResponseEntity<>(httpApiResponse, HttpStatus.OK);
    }
}