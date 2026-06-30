package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicemongodb.entity.*;
import com.primesys.adminservicemongodb.repository.IssueDataRepository;
import com.primesys.adminservicemongodb.repository.WorkflowStepRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final IssueDataRepository issueRepo;
    private final WorkflowStepRepository stepRepo;

    // @Value("${app.workflow.defaultExpiryMinutes}")
    private Integer defaultExpiryMinutes = 5;

    // @Value("${app.workflow.escalationStepId}")
    private Integer escalationStepId = 99;

    // Start or resume; returns current step (system text already appended on first start)
    public WorkflowStep startOrResume(String issueId) {
        IssueEntity issue = fetchIssue(issueId);

        if (issue.getCurrentStepId() == null) {
            // start at step 1
            WorkflowStep first = stepRepo.findById(1).orElse(null);
            if (first != null) {
                issue.setCurrentStepId(first.getId());
                String msg = renderMessage(first, issue);
                issue.getComments().add(Comment.create(msg, "system-bot", "SYSTEM", first.getId()));
                issue.setWorkflowExpiry(Instant.now().plusSeconds(defaultExpiryMinutes * 60L).toEpochMilli());
                issue.setUpdatedAt(System.currentTimeMillis());
                issueRepo.save(issue);
            }
            return first;
        }

        // check expiry → escalate if necessary
        Long expiry = issue.getWorkflowExpiry();
        if (expiry != null && expiry < System.currentTimeMillis()) {
            issue.setEscalationStatus("ESCALATED");
            issue.setIssueStatus("ESCALATED");
            issue.getComments().add(
                    Comment.create("Case auto-escalated due to timeout", "system-bot", "SYSTEM", escalationStepId));
            issueRepo.save(issue);
            return stepRepo.findById(escalationStepId).orElse(null);
        }

        return stepRepo.findById(issue.getCurrentStepId()).orElse(null);
    }

    // process user's reply: save comment, resolve next step using conditions, append system message
    public IssueEntity processUserReply(String issueId, String userId, String reply) {
        IssueEntity issue = fetchIssue(issueId);
        Integer curStepId = issue.getCurrentStepId();
        WorkflowStep currentStep = stepRepo.findById(curStepId)
                .orElseThrow(() -> new IllegalStateException("Current step not found"));

        // save user input as comment
        Comment userComment = Comment.create(reply, userId, "USER", currentStep.getId());
        issue.getComments().add(userComment);

        // Update mapping used for rules
        Map<String, String> inputs = extractUserInputs(issue);

        // Decide next step using conditions
        Integer nextStepId = resolveNextStep(currentStep, inputs);

        if (nextStepId != null) {
            WorkflowStep next = stepRepo.findById(nextStepId)
                    .orElseThrow(() -> new IllegalStateException("Next step not found"));
            String finalMessage = renderMessage(next, issue);

            // append system message
            Comment systemComment = Comment.create(finalMessage, "system-bot", "SYSTEM", next.getId());
            issue.getComments().add(systemComment);

            // update issue
            issue.setCurrentStepId(next.getId());
            issue.setWorkflowExpiry(Instant.now().plusSeconds(defaultExpiryMinutes * 60L).toEpochMilli());
        } else {
            // workflow finished
            issue.setCurrentStepId(null);
            issue.setIssueStatus("CLOSED");
            issue.setWorkflowExpiry(null);
            issue.getComments().add(Comment.create("Workflow completed", "system-bot", "SYSTEM", null));
        }

        issue.setUpdatedAt(System.currentTimeMillis());
        return issueRepo.save(issue);
    }

    // resume and skip already-answered steps and re-render system prompt using placeholders
    public IssueEntity resumeAndSkipAnswered(String issueId) {
        IssueEntity issue = fetchIssue(issueId);
        if ("CLOSED".equalsIgnoreCase(issue.getIssueStatus()))
            return issue;

        Integer currentStepId = issue.getCurrentStepId();
        if (currentStepId == null)
            return issue;

        WorkflowStep step = stepRepo.findById(currentStepId)
                .orElseThrow(() -> new IllegalStateException("Step not found"));

        boolean answered = issue.getComments().stream()
                .anyMatch(c -> "USER".equals(c.getRole()) && Objects.equals(c.getStepId(), step.getId()));

        if (answered) {
            // compute next based on stored inputs
            Map<String, String> inputs = extractUserInputs(issue);
            Integer nextStepId = resolveNextStep(step, inputs);
            if (nextStepId != null) {
                WorkflowStep next = stepRepo.findById(nextStepId).orElseThrow();
                String finalMessage = renderMessage(next, issue);
                // add system message
                issue.getComments().add(Comment.create(finalMessage, "system-bot", "SYSTEM", next.getId()));
                issue.setCurrentStepId(next.getId());
                issue.setWorkflowExpiry(Instant.now().plusSeconds(defaultExpiryMinutes * 60L).toEpochMilli());
            } else {
                issue.setCurrentStepId(null);
                issue.setIssueStatus("CLOSED");
                issue.getComments().add(Comment.create("Workflow completed", "system-bot", "SYSTEM", null));
            }
            issue.setUpdatedAt(System.currentTimeMillis());
            return issueRepo.save(issue);
        } else {
            // re-prompt current step (render with placeholders)
            String finalMessage = renderMessage(step, issue);
            // ensure system message present for this step
            boolean sysExists = issue.getComments().stream()
                    .anyMatch(c -> "SYSTEM".equals(c.getRole()) && Objects.equals(c.getStepId(), step.getId()));
            if (!sysExists) {
                issue.getComments().add(Comment.create(finalMessage, "system-bot", "SYSTEM", step.getId()));
            }
            issue.setUpdatedAt(System.currentTimeMillis());
            return issueRepo.save(issue);
        }
    }

    // --- helpers ---

    private IssueEntity fetchIssue(String issueId) {
        ObjectId oid = new ObjectId(issueId);
        return issueRepo.findById(String.valueOf(oid))
                .orElseThrow(() -> new IllegalArgumentException("Issue not found: " + issueId));
    }

    // Extract user inputs mapping by placeholder names.
    // IMPORTANT: mapping logic depends on your step->variable mapping policy.
    // Here we map by step placeholders if present, else use simple conventions.
    private Map<String, String> extractUserInputs(IssueEntity issue) {
        Map<String, String> inputs = new HashMap<>();

        // strategy:
        // for every USER comment that has a stepId, try to map that stepId to the placeholders of that step
        List<Comment> userComments = issue.getComments().stream()
                .filter(c -> "USER".equals(c.getRole()) && c.getStepId() != null).collect(Collectors.toList());

        for (Comment c : userComments) {
            // fetch the step to know placeholders (if any)
            stepRepo.findById(c.getStepId()).ifPresent(step -> {
                if (step.getPlaceholders() != null && !step.getPlaceholders().isEmpty()) {
                    // if step defined placeholders, map first placeholder -> comment text
                    // (you can adjust mapping logic to multiple placeholders)
                    String placeholder = step.getPlaceholders().get(0);
                    inputs.put(placeholder, c.getMessage());
                } else {
                    // fallback mapping by step id -> canonical names (example)
                    // customize mapping as per your workflow design
                    if (c.getStepId() == 1)
                        inputs.put("division", c.getMessage());
                    else if (c.getStepId() == 2)
                        inputs.put("section", c.getMessage());
                    else if (c.getStepId() == 3)
                        inputs.put("contact", c.getMessage());
                }
            });
        }
        return inputs;
    }

    // Render message by replacing placeholders {name} with saved input values
    private String renderMessage(WorkflowStep step, IssueEntity issue) {
        String message = step.getMessage();
        if (message == null)
            return "";
        Map<String, String> inputs = extractUserInputs(issue);
        for (Map.Entry<String, String> e : inputs.entrySet()) {
            String key = e.getKey();
            String val = e.getValue();
            if (val != null) {
                message = message.replace("{" + key + "}", val);
            }
        }
        return message;
    }

    // Resolve next step using conditions (supports EQ and CONTAINS), else defaultNextStep
    private Integer resolveNextStep(WorkflowStep step, Map<String, String> inputs) {
        if (step.getConditions() != null) {
            for (WorkflowCondition cond : step.getConditions()) {
                String field = cond.getField();
                String operator = cond.getOperator() == null ? "EQ" : cond.getOperator().toUpperCase();
                String expected = cond.getValue();
                String actual = inputs.get(field);
                if (actual == null)
                    continue;
                boolean match = false;
                if ("EQ".equals(operator)) {
                    match = actual.equalsIgnoreCase(expected);
                } else if ("CONTAINS".equals(operator)) {
                    match = actual.toLowerCase().contains(expected.toLowerCase());
                }
                if (match)
                    return cond.getNextStep();
            }
        }
        return step.getDefaultNextStep();
    }
}
