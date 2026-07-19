package com.coderolls.jobplatform.schedulerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdhocRunRequest {

    @NotBlank
    private String jobName;

    @NotBlank
    private String jobGroup;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "businessDate must be in yyyy-MM-dd format")
    private String businessDate;
}