package com.coderolls.jobplatform.schedulerservice.controller;

import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.ConcurrencyPolicy;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import com.coderolls.jobplatform.schedulerservice.domain.enums.TriggerType;
import com.coderolls.jobplatform.schedulerservice.dto.AdhocRunRequest;
import com.coderolls.jobplatform.schedulerservice.repository.InactiveJobRepository;
import com.coderolls.jobplatform.schedulerservice.service.JobInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class AdhocRunController {

    private final Scheduler quartzScheduler;
    private final InactiveJobRepository inactiveJobRepository;
    private final JobInstanceService jobInstanceService;

    @Operation(summary = "Manually trigger a job for a given business date")
    @PostMapping("/adhoc-run")
    public ResponseEntity<?> adhocRun(@RequestBody @Valid AdhocRunRequest request) throws SchedulerException {
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());
        JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);

        if (jobDetail == null) {
            return ResponseEntity.badRequest()
                    .body("No such job: " + request.getJobName() + " in group " + request.getJobGroup());
        }
        if (inactiveJobRepository.existsByJobName(request.getJobName())) {
            return ResponseEntity.status(409).body("Job [" + request.getJobName() + "] is inactive");
        }

        JobDataMap dataMap = jobDetail.getJobDataMap();
        ConcurrencyPolicy policy = ConcurrencyPolicy.valueOf(dataMap.getString("concurrencyPolicy"));

        if (policy == ConcurrencyPolicy.SINGLE_INSTANCE
                && jobInstanceService.hasNonTerminalInstance(request.getJobName(), request.getBusinessDate())) {
            return ResponseEntity.status(409).body(
                    "Job [" + request.getJobName() + "] already has a non-terminal instance for "
                            + request.getBusinessDate() + ". Use POST /api/jobs/instances/{instanceId}/force-fail "
                            + "to clear it first if this is stuck.");
        }

        JobInstance instance = jobInstanceService.createInstance(
                request.getJobName(), request.getJobGroup(), dataMap, request.getBusinessDate(),
                JobStatus.CREATED, TriggerType.MANUAL);

        jobInstanceService.attemptDispatch(instance);
        return ResponseEntity.ok(instance);
    }
}