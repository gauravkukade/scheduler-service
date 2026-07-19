package com.coderolls.jobplatform.schedulerservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InactivateJobResponse {
    private String jobName;
    private boolean success;
    private String message;
}