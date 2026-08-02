package com.coderolls.jobplatform.schedulerservice.controller;

import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.ConcurrencyPolicy;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import com.coderolls.jobplatform.schedulerservice.domain.enums.TriggerType;
import com.coderolls.jobplatform.schedulerservice.dto.AdhocRunRequest;
import com.coderolls.jobplatform.schedulerservice.dto.DispatchRequest;
import com.coderolls.jobplatform.schedulerservice.repository.InactiveJobRepository;
import com.coderolls.jobplatform.schedulerservice.service.JobDispatchService;
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
    private final JobDispatchService jobDispatchService;

    @Operation(summary = "Manually trigger a job for a given business date")
    @PostMapping("/adhoc-run")
    public ResponseEntity<?> adhocRun(@RequestBody @Valid AdhocRunRequest request) throws SchedulerException {
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());
        JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);

        if (jobDetail == null) {
            return ResponseEntity.badRequest()
                    .body("No such job: " + request.getJobName() + " in group " + request.getJobGroup());
        }
        JobInstance instance = jobDispatchService.dispatch(
                new DispatchRequest(
                        request.getJobName(),
                        request.getJobGroup(),
                        request.getBusinessDate(),
                        TriggerType.MANUAL,
                        null),
                jobDetail.getJobDataMap());

        return ResponseEntity.ok(instance);
    }
}