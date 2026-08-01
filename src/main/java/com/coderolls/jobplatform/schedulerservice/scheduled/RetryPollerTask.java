package com.coderolls.jobplatform.schedulerservice.scheduled;

import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import com.coderolls.jobplatform.schedulerservice.repository.JobInstanceRepository;
import com.coderolls.jobplatform.schedulerservice.service.JobInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

// NOT A QUARTZ JOB, SIMPLE PRING SCHEDULED JOB
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryPollerTask {

    private final JobInstanceRepository jobInstanceRepository;
    private final JobInstanceService jobInstanceService;

    @Scheduled(fixedDelayString = "${retry.poller.fixed-delay-ms:15000}")
    public void pollAndRetry() {
        List<JobInstance> due = jobInstanceRepository
                .findByJobStatusAndNextRetryAtLessThanEqual(JobStatus.PROCESSOR_UNAVAILABLE, Instant.now());

        for (JobInstance instance : due) {
            if (instance.getRetryCount() >= instance.getMaxRetryAttempts()) {
                log.warn("Instance [{}] exceeded max retry attempts ({}), marking MAX_RETRIES_EXCEEDED",
                        instance.getInstanceId(), instance.getMaxRetryAttempts());
                jobInstanceService.transitionTo(instance, JobStatus.MAX_RETRIES_EXCEEDED);
                continue;
            }
            instance.setRetryCount(instance.getRetryCount() + 1);
            log.info("Retrying instance [{}], attempt {}/{}",
                    instance.getInstanceId(), instance.getRetryCount(), instance.getMaxRetryAttempts());
            jobInstanceService.attemptDispatch(instance);
        }
    }
}