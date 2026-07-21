package com.coderolls.jobplatform.schedulerservice.quartz;

import com.coderolls.jobplatform.schedulerservice.domain.enums.ConcurrencyPolicy;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.TriggerType;
import com.coderolls.jobplatform.schedulerservice.repository.InactiveJobRepository;
import com.coderolls.jobplatform.schedulerservice.service.JobInstanceService;
import com.coderolls.jobplatform.schedulerservice.util.BusinessDateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobDispatcher implements Job {

    private final InactiveJobRepository inactiveJobRepository;
    private final JobInstanceService jobInstanceService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getJobDetail().getKey();
        String jobName = jobKey.getName();
        String jobGroup = jobKey.getGroup();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        if (inactiveJobRepository.existsByJobName(jobName)) {
            log.info("Job [{}] is inactive, skipping dispatch", jobName);
            return;
        }

        String businessDate = BusinessDateUtil.todayMinus1();

        ConcurrencyPolicy policy = ConcurrencyPolicy.valueOf(dataMap.getString("concurrencyPolicy"));
        if (policy == ConcurrencyPolicy.SINGLE_INSTANCE
                && jobInstanceService.hasNonTerminalInstance(jobName, businessDate)) {
            log.info("Job [{}] already has a non-terminal instance for {}, skipping (SINGLE_INSTANCE)",
                    jobName, businessDate);
            jobInstanceService.createInstance(jobName, jobGroup, dataMap, businessDate, JobStatus.SKIPPED_CONCURRENT, TriggerType.SCHEDULED);
            return;
        }

        JobInstance instance = jobInstanceService.createInstance(jobName, jobGroup, dataMap, businessDate, JobStatus.CREATED, TriggerType.SCHEDULED);
        jobInstanceService.attemptDispatch(instance);
    }
}