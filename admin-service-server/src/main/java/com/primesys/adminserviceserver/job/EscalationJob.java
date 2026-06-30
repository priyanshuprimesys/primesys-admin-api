package com.primesys.adminserviceserver.job;

import com.primesys.adminservicemongodb.entity.IssueEntity;
import com.primesys.adminservicemongodb.repository.IssueDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EscalationJob {

    private final IssueDataRepository issueRepo;

    // @Scheduled(cron = "0 */5 * * * *") // every 5 minutes
    public void sweep() {
        long now = Instant.now().toEpochMilli();
        List<IssueEntity> issues = issueRepo.findByWorkflowExpiryLessThanAndEscalationStatusNot(now, "ESCALATED");
        int escalated = 0;
        for (IssueEntity issue : issues) {
            issue.setEscalationStatus("ESCALATED");
            issue.setIssueStatus("ESCALATED");
            issue.getComments().add(com.primesys.adminservicemongodb.entity.Comment
                    .create("Case auto-escalated due to timeout", "system-bot", "SYSTEM", null));
            issueRepo.save(issue);
            escalated++;
        }
        if (escalated > 0)
            System.out.println("Auto-escalated " + escalated + " cases");
    }
}
