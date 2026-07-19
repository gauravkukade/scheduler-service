package com.coderolls.jobplatform.schedulerservice.dto;

import com.coderolls.jobplatform.schedulerservice.domain.enums.JobGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InactivateJobRequest {
    @NotBlank
    private String jobName;
    @NotNull
    private
    JobGroup jobGroup;
    @NotBlank
    private String reason;
}