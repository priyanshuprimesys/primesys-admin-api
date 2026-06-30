package com.primesys.adminserviceserver.job;

import com.primesys.adminservicemongodb.entity.WorkflowCondition;
import com.primesys.adminservicemongodb.entity.WorkflowStep;
import com.primesys.adminservicemongodb.repository.WorkflowStepRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WorkflowInitializer {

    private final WorkflowStepRepository stepRepo;

    @PostConstruct
    public void init() {
        if (stepRepo.count() > 0)
            return;

        // Step 1: Ask division (maps placeholder "division")
        stepRepo.save(
                WorkflowStep.builder().id(1).message("GPS is showing OFF. Sir, are you calling from which division?")
                        .defaultNextStep(2).placeholders(List.of("division")).build());

        // Step 2: Ask section; branch based on division value
        stepRepo.save(WorkflowStep.builder().id(2).message("Which section are you calling from?").defaultNextStep(3)
                .conditions(List.of(
                        WorkflowCondition.builder().field("division").operator("EQ").value("XYZ").nextStep(5).build(),
                        WorkflowCondition.builder().field("division").operator("CONTAINS").value("north").nextStep(6)
                                .build()))
                .placeholders(List.of("section")).build());

        // Step 3: Request user contact or ask to turn on GPS
        stepRepo.save(WorkflowStep.builder().id(3).message(
                "Okay sir, please share the user contact number or turn ON the GPS and keep it in an open area.")
                .defaultNextStep(4).placeholders(List.of("contact")).build());

        // Step 4: Acknowledge
        stepRepo.save(WorkflowStep.builder().id(4)
                .message("Thank you. Our team will check the GPS and update you shortly.").defaultNextStep(10).build());

        // backend steps 10..16 (linear)
        stepRepo.save(WorkflowStep.builder().id(10).message("Login to division portal").defaultNextStep(11).build());
        stepRepo.save(WorkflowStep.builder().id(11).message("Check GPS status in Primesys web portal")
                .defaultNextStep(12).build());
        stepRepo.save(WorkflowStep.builder().id(12).message("Check PERIOD#; if wrong set Day/24hrs").defaultNextStep(13)
                .build());
        stepRepo.save(WorkflowStep.builder().id(13).message("Send commands: GPSON#, HBT,5#, TIMER,5,60#, APN")
                .defaultNextStep(14).build());
        stepRepo.save(WorkflowStep.builder().id(14).message("Check calling (SOS#, FN#) [Optional]").defaultNextStep(15)
                .build());
        stepRepo.save(WorkflowStep.builder().id(15).message("Verify updated location in Primesys Web App")
                .defaultNextStep(16).build());
        stepRepo.save(WorkflowStep.builder().id(16).message("Take screenshot & share on WhatsApp").defaultNextStep(null)
                .build());

        // escalation step
        stepRepo.save(WorkflowStep.builder().id(99).message("Escalate issue").defaultNextStep(null).build());
    }
}
