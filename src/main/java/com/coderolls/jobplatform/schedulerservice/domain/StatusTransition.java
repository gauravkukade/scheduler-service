package com.coderolls.jobplatform.schedulerservice.domain;

import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusTransition {
    private JobStatus jobStatus;
    private Instant enteredAt;
    private Instant exitedAt;   // null while this is the current/active status
}