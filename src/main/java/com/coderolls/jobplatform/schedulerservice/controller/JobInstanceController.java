package com.coderolls.jobplatform.schedulerservice.controller;

import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import com.coderolls.jobplatform.schedulerservice.dto.JobCompletionEvent;
import com.coderolls.jobplatform.schedulerservice.repository.JobInstanceRepository;
import com.coderolls.jobplatform.schedulerservice.service.JobCompletionService;
import com.coderolls.jobplatform.schedulerservice.service.JobInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs/instances")
@RequiredArgsConstructor
public class JobInstanceController {

    private final JobInstanceRepository jobInstanceRepository;
    private final JobInstanceService jobInstanceService;
    private final JobCompletionService jobCompletionService;

    @Operation(summary = "Get full details of a job instance")
    @GetMapping("/{instanceId}")
    public ResponseEntity<JobInstance> getInstance(@PathVariable String instanceId) {
        return jobInstanceRepository.findByInstanceId(instanceId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get only the current status of a job instance")
    @GetMapping("/{instanceId}/jobStatus")
    public ResponseEntity<JobStatus> getJobStatus(@PathVariable String instanceId) {
        return jobInstanceRepository.findByInstanceId(instanceId)
                .map(instance -> ResponseEntity.ok(instance.getJobStatus()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Force a stuck (non-terminal) instance to a terminal status. Only FAILED is currently supported.")
    @PostMapping("/{instanceId}/jobStatus/{jobStatus}")
    public ResponseEntity<?> forceFail(@PathVariable String instanceId,
                                       @Parameter(schema = @Schema(allowableValues = {"FAILED"}))
                                       @PathVariable() JobStatus jobStatus) {
        try {
            JobInstance updated = jobInstanceService.forceFail(instanceId, jobStatus);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/completion")
    public ResponseEntity<Void> jobCompleted(@RequestBody JobCompletionEvent jobCompletionEvent) throws SchedulerException {
        jobCompletionService.onJobCompletion(jobCompletionEvent);
        return ResponseEntity.ok().build();
    }
}