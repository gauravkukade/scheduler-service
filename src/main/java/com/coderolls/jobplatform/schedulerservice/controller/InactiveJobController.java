package com.coderolls.jobplatform.schedulerservice.controller;

import com.coderolls.jobplatform.schedulerservice.domain.InactiveJob;
import com.coderolls.jobplatform.schedulerservice.dto.InactivateJobRequest;
import com.coderolls.jobplatform.schedulerservice.dto.InactivateJobResponse;
import com.coderolls.jobplatform.schedulerservice.repository.InactiveJobRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/jobs/inactive")
@RequiredArgsConstructor
public class InactiveJobController {

    private final InactiveJobRepository inactiveJobRepository;

    @PostMapping
    public ResponseEntity<InactivateJobResponse> inactivate(@RequestBody @Valid InactivateJobRequest req) {
        InactiveJob job = InactiveJob.builder()
                .jobName(req.getJobName())
                .jobGroup(req.getJobGroup())
                .reason(req.getReason())
                .deactivatedBy("SYSTEM") // TODO replace with authenticated user once security is added
                .deactivatedAt(Instant.now())
                .build();
        inactiveJobRepository.save(job);
        return ResponseEntity.ok(InactivateJobResponse.builder()
                .jobName(req.getJobName()).success(true).message("Job marked inactive").build());
    }
}