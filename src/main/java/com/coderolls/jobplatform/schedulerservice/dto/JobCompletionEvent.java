package com.coderolls.jobplatform.schedulerservice.dto;

import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class JobCompletionEvent {

    private String instanceId;

    private JobStatus jobStatus;

    private Instant completedAt;
}