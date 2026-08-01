package com.coderolls.jobplatform.schedulerservice.repository;

import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import com.coderolls.jobplatform.schedulerservice.domain.enums.JobStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface JobInstanceRepository extends MongoRepository<JobInstance, String> {
    Optional<JobInstance> findByInstanceId(String instanceId);

    List<JobInstance> findByJobNameAndBusinessDateAndJobStatusIn(
            String jobName, String businessDate, List<JobStatus> statuses);

    List<JobInstance> findByJobStatusAndNextRetryAtLessThanEqual(JobStatus status, Instant now);
}