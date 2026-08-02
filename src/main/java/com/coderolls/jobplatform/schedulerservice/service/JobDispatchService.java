package com.coderolls.jobplatform.schedulerservice.service;

import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.ConcurrencyPolicy;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import com.coderolls.jobplatform.schedulerservice.domain.enums.TriggerType;
import com.coderolls.jobplatform.schedulerservice.dto.DispatchRequest;
import com.coderolls.jobplatform.schedulerservice.repository.InactiveJobRepository;
import com.coderolls.jobplatform.schedulerservice.util.BusinessDateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobDispatchService {

    private final InactiveJobRepository inactiveJobRepository;
    private final JobInstanceService jobInstanceService;

    public JobInstance dispatch(DispatchRequest dispatchRequest, JobDataMap jobDataMap) {

        String jobName = dispatchRequest.jobName();
        String jobGroup = dispatchRequest.jobGroup();
        String businessDate = dispatchRequest.businessDate();
        TriggerType triggerType = dispatchRequest.triggerType();

        log.info("Dispatch request received for job [{}], businessDate [{}], triggerType [{}], parentJobInstanceId [{}]",
                jobName, businessDate, triggerType, dispatchRequest.parentJobInstanceId());

        if (inactiveJobRepository.existsByJobName(jobName)) {
            log.info("Job [{}] is inactive, skipping dispatch", jobName);
            throw new IllegalStateException("Job [" + jobName + "] is inactive");
        }

        ConcurrencyPolicy policy = ConcurrencyPolicy.valueOf(jobDataMap.getString("concurrencyPolicy"));
        if (policy == ConcurrencyPolicy.SINGLE_INSTANCE
                && jobInstanceService.hasNonTerminalInstance(jobName, businessDate)) {
            log.info("Job [{}] already has a non-terminal instance for {}, skipping (SINGLE_INSTANCE)",
                    jobName, businessDate);
            JobInstance instance = jobInstanceService.createInstance(jobName, jobGroup, jobDataMap, businessDate, JobStatus.SKIPPED_CONCURRENT, triggerType);
            if (dispatchRequest.parentJobInstanceId() != null) {
                instance.setParentJobInstanceId(dispatchRequest.parentJobInstanceId());
            }
            return instance;
        }

        JobInstance instance = jobInstanceService.createInstance(jobName, jobGroup, jobDataMap, businessDate, JobStatus.CREATED, triggerType);
        if (dispatchRequest.parentJobInstanceId() != null) {
            instance.setParentJobInstanceId(dispatchRequest.parentJobInstanceId());
        }

        jobInstanceService.attemptDispatch(instance);

        return instance;
    }
}
