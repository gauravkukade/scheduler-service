package com.coderolls.jobplatform.schedulerservice.service;

import com.coderolls.jobplatform.schedulerservice.client.ProcessorClient;
import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.StatusTransition;
import com.coderolls.jobplatform.schedulerservice.domain.enums.*;
import com.coderolls.jobplatform.schedulerservice.repository.JobInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.JobDataMap;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobInstanceService {

    private final JobInstanceRepository jobInstanceRepository;
    private final ProcessorClient processorClient;

    public static final List<JobStatus> NON_TERMINAL = List.of(
            JobStatus.CREATED, JobStatus.DISPATCHING, JobStatus.READY,
            JobStatus.PROCESSING, JobStatus.PROCESSOR_UNAVAILABLE, JobStatus.RETRY_SCHEDULED);

    public boolean hasNonTerminalInstance(String jobName, String businessDate) {
        return !jobInstanceRepository
                .findByJobNameAndBusinessDateAndStatusIn(jobName, businessDate, NON_TERMINAL)
                .isEmpty();
    }

    public JobInstance createInstance(String jobName, String jobGroup, JobDataMap dataMap, String businessDate,
                                      JobStatus initialStatus, TriggerType triggerType) {
        Instant now = Instant.now();
        JobInstance instance = JobInstance.builder()
                .instanceId(UUID.randomUUID().toString())
                .jobName(jobName)
                .jobGroup(jobGroup)
                .businessDate(businessDate)
                .triggerType(triggerType)
                .attemptNumber(1)
                .status(initialStatus)
                .statusHistory(new ArrayList<>(List.of(
                        StatusTransition.builder().status(initialStatus).enteredAt(now).build())))
                .retryCount(0)
                .maxRetryAttempts(Integer.parseInt(dataMap.getString("maxRetryAttempts")))
                .retryBackoffSeconds(Integer.parseInt(dataMap.getString("retryBackoffSeconds")))
                .triggeredBy("SYSTEM")
                .build();
        return jobInstanceRepository.save(instance);
    }

    public JobInstance transitionTo(JobInstance instance, JobStatus newStatus) {
        Instant now = Instant.now();
        List<StatusTransition> history = instance.getStatusHistory();
        if (!history.isEmpty()) {
            history.get(history.size() - 1).setExitedAt(now);
        }
        history.add(StatusTransition.builder().status(newStatus).enteredAt(now).build());
        instance.setStatus(newStatus);
        return jobInstanceRepository.save(instance);
    }

    /** Attempts (or re-attempts) dispatch to Processor, updating status accordingly. */
    public void attemptDispatch(JobInstance instance) {
        transitionTo(instance, JobStatus.DISPATCHING);
        boolean accepted = processorClient.dispatch(instance);
        if (!accepted) {
            instance.setLastAttemptAt(Instant.now());
            instance.setNextRetryAt(Instant.now().plusSeconds(instance.getRetryBackoffSeconds()));
            transitionTo(instance, JobStatus.PROCESSOR_UNAVAILABLE);
        } else {
            transitionTo(instance, JobStatus.READY);
        }
    }

    public JobInstance forceFail(String instanceId, JobStatus requestedStatus) {
        if (requestedStatus != JobStatus.FAILED) {
            throw new IllegalArgumentException(
                    "Only FAILED is supported via this endpoint. Requested: " + requestedStatus);
        }

        JobInstance instance = jobInstanceRepository.findByInstanceId(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("No instance found: " + instanceId));

        instance.setFailureReason("Manually marked as FAILED via force-fail API");
        instance.setFailureCategory(FailureCategory.TECHNICAL);
        return transitionTo(instance, JobStatus.FAILED);
    }
}