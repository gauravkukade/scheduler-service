package com.coderolls.jobplatform.schedulerservice.domain;

import com.coderolls.jobplatform.schedulerservice.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "job_instances")
public class JobInstance {

    @Id
    private String id;

    @Indexed(unique = true)
    private String instanceId;   // UUID, generated at creation

    @Indexed
    private String jobName;      // unique per definition, matches jobs.xml job-name

    @Indexed
    private JobGroup jobGroup;
    private SourceSystem sourceSystem;
    private Product product;
    private Region region;
    private Jurisdiction jurisdiction;

    @Indexed
    private String businessDate; // yyyy-MM-dd, always UTC-based

    private TriggerType triggerType;
    private String parentJobInstanceId;
    private String rerunOfInstanceId;
    private int attemptNumber;

    @Indexed
    private JobStatus status;                    // current status — denormalized for fast filtering
    private List<StatusTransition> statusHistory; // full trail

    private String failureReason;
    private FailureCategory failureCategory;

    private int retryCount;
    private Instant nextRetryAt;
    private Instant lastAttemptAt;
    private int maxRetryAttempts;
    private int retryBackoffSeconds;

    private String triggeredBy;
}