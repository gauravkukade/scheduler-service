package com.coderolls.jobplatform.schedulerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InactivateJobRequest {
    @NotBlank
    private String jobName;
    @NotNull
    private
    String jobGroup;
    @NotBlank
    private String reason;
}