package com.coderolls.jobplatform.schedulerservice.quartz;

import com.coderolls.jobplatform.schedulerservice.domain.enums.ConcurrencyPolicy;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.TriggerType;
import com.coderolls.jobplatform.schedulerservice.dto.DispatchRequest;
import com.coderolls.jobplatform.schedulerservice.repository.InactiveJobRepository;
import com.coderolls.jobplatform.schedulerservice.service.JobDispatchService;
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


    private final JobDispatchService jobDispatchService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getJobDetail().getKey();
        String jobName = jobKey.getName();
        String jobGroup = jobKey.getGroup();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();

        String businessDate = BusinessDateUtil.todayMinus1();

        jobDispatchService.dispatch(new DispatchRequest(jobName, jobGroup, businessDate, TriggerType.SCHEDULED, null), dataMap);
    }
}