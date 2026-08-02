package com.coderolls.jobplatform.schedulerservice.dto;

import com.coderolls.jobplatform.schedulerservice.domain.enums.TriggerType;

public record DispatchRequest(
        String jobName,
        String jobGroup,
        String businessDate,
        TriggerType triggerType,
        String parentJobInstanceId
) {}