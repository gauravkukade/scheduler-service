package com.coderolls.jobplatform.schedulerservice.service;

import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import com.coderolls.jobplatform.schedulerservice.domain.enums.TriggerType;
import com.coderolls.jobplatform.schedulerservice.dto.DispatchRequest;
import com.coderolls.jobplatform.schedulerservice.dto.JobCompletionEvent;
import com.coderolls.jobplatform.schedulerservice.repository.JobInstanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCompletionService {

    private final Scheduler quartzScheduler;
    private final JobInstanceRepository jobInstanceRepository;
    private final JobDispatchService jobDispatchService;

    public void onJobCompletion(JobCompletionEvent event) throws SchedulerException {
        if (!event.getJobStatus().isTerminal()) {
            log.warn("Ignoring non-terminal completion callback for instance [{}] with status [{}]",
                    event.getInstanceId(), event.getJobStatus());
            return;
        }

        if (event.getJobStatus() != JobStatus.SUCCESS) {
            log.warn("Job [{}] completed with status [{}]. No downstream job will be triggered.",
                    event.getInstanceId(), event.getJobStatus());
            return;
        }

        JobInstance instance = jobInstanceRepository.findByInstanceId(event.getInstanceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No JobInstance found for instanceId: " + event.getInstanceId()));

        log.info("Received completion callback for JobInstance [{}], job [{}], status [{}]",
                instance.getInstanceId(), instance.getJobName(), event.getJobStatus());

        JobKey jobKey = JobKey.jobKey(instance.getJobName(), instance.getJobGroup());
        JobDetail jobDetail = getJobDetail(jobKey);

        if (jobDetail == null) {
            log.error("Quartz JobDetail not found for job [{}] in group [{}]", instance.getJobName(), instance.getJobGroup());
            return;
        }

        JobDataMap dataMap = jobDetail.getJobDataMap();
        String nextJobName = dataMap.getString("nextJobName");

        if (nextJobName == null || nextJobName.isBlank()) {
            log.info("Job [{}] has no nextJobName configured.", instance.getJobName());
            return;
        }

        nextJobName = nextJobName.trim();

        JobKey nextJobKey = JobKey.jobKey(nextJobName, instance.getJobGroup());
        JobDetail nextJobDetail = getJobDetail(nextJobKey);

        if (nextJobDetail == null) {
            log.error("Next job [{}] not found in group [{}]. Cannot dispatch.", nextJobName, instance.getJobGroup());
            return;
        }

        log.info("Found nextJobName [{}] configured for job [{}]", nextJobName, instance.getJobName());
        log.info( "Dispatching dependent job [{}] after successful completion of job [{}] for JobInstance [{}]",
                nextJobName, instance.getJobName(), instance.getInstanceId());

        jobDispatchService.dispatch(
                new DispatchRequest(
                        nextJobName,
                        instance.getJobGroup(),
                        instance.getBusinessDate(),
                        TriggerType.DEPENDENCY,
                        instance.getInstanceId()),
                nextJobDetail.getJobDataMap());
    }


    private JobDetail getJobDetail(JobKey jobKey) throws SchedulerException {
        JobDetail jobDetail = quartzScheduler.getJobDetail(jobKey);

        if (jobDetail == null) {
            throw new IllegalArgumentException("Quartz JobDetail not found for job: " + jobKey);
        }

        return jobDetail;
    }
}
