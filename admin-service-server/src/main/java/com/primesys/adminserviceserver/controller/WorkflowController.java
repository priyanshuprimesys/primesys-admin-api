package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicemongodb.entity.Comment;
import com.primesys.adminservicemongodb.entity.IssueEntity;
import com.primesys.adminservicemongodb.entity.WorkflowStep;
import com.primesys.adminserviceserver.service.impl.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    // Start or resume a workflow for the issue and return current step
    @PostMapping("/issues/{issueId}/startOrResume")
    public WorkflowStep startOrResume(@PathVariable String issueId) {
        return workflowService.startOrResume(issueId);
    }

    // Resume and skip answered steps (re-renders system prompt or skip to next)
    @PostMapping("/issues/{issueId}/resume")
    public IssueEntity resume(@PathVariable String issueId) {
        return workflowService.resumeAndSkipAnswered(issueId);
    }

    // User replies (saves input, resolves next step)
    @PostMapping("/issues/{issueId}/reply")
    public IssueEntity reply(@PathVariable String issueId, @RequestParam String userId, @RequestParam String reply) {
        return workflowService.processUserReply(issueId, userId, reply);
    }

    // Add an arbitrary comment (agent/system)
    // @PostMapping("/issues/{issueId}/comments")
    // public IssueEntity addComment(@PathVariable String issueId, @RequestBody Comment comment) {
    // return workflowService.addComment(issueId, comment);
    // }
    //
    // // Get issue by id (full conversation)
    // @GetMapping("/issues/{issueId}")
    // public IssueEntity getIssue(@PathVariable String issueId) {
    // return workflowService.fetchIssuePublic(issueId);
    // }
}
