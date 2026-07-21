package com.coderolls.jobplatform.schedulerservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "inactive_jobs")
public class InactiveJob {

    @Id
    private String id;

    @Indexed(unique = true)
    private String jobName;

    private String jobGroup;
    private String reason;          // one-liner, why it was deactivated
    private String deactivatedBy;
    private Instant deactivatedAt;
}